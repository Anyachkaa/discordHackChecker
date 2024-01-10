package ru.itskekoff.hackchecker.bot.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;
import ru.itskekoff.hackchecker.bot.configuration.BotMode;
import ru.itskekoff.hackchecker.bot.utils.FileUtils;
import ru.itskekoff.hackchecker.bot.utils.embed.CustomMessageEmbed;
import ru.itskekoff.hackchecker.bot.utils.types.BannedUser;
import ru.itskekoff.hackchecker.framework.types.ScanResult;
import ru.itskekoff.hackchecker.bot.configuration.Settings;
import ru.itskekoff.hackchecker.framework.types.exception.InvalidPluginException;
import ru.itskekoff.hackchecker.bot.utils.embed.EmbedColorSorting;
import ru.itskekoff.hackchecker.bot.utils.OtherUtils;
import ru.itskekoff.hackchecker.bot.utils.embed.EmbedMessageBuilder;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.List;

import static ru.itskekoff.hackchecker.bot.PluginScanBot.*;

public class ChannelMessageListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Message message = event.getMessage();
        BotMode mode = Settings.IMP.MAIN.BOT_MODE;

        if (mode != BotMode.SIMULTANEOUS) {
            if ((mode == BotMode.GUILD && !message.isFromGuild()) || (mode == BotMode.DIRECT && message.isFromGuild())) {
                return;
            }
        }

        if (message.getAttachments().isEmpty() ||
            message.getAttachments().stream().noneMatch(attachment -> attachment.getFileName().endsWith(".jar"))) {
            return;
        }

        List<Message.Attachment> attachmentList = message.getAttachments();

        if (message.isFromGuild() && (mode == BotMode.GUILD || mode == BotMode.SIMULTANEOUS) &&
            !database.isUserAndChannelCorrect(message.getAuthor(), message.getChannel())) {
            return;
        }
        BannedUser bannedUser = database.getBannedUser(message.getAuthor().getIdLong());
        if (bannedUser != null && !bannedUser.isExpired()) {
            sendBanMessage(message, bannedUser.expirationToSeconds());
            return;
        }

        Message startedProcessing;
        try {
            startedProcessing = message.reply(MessageCreateData.fromEmbeds(
                            EmbedMessageBuilder.buildNotify(Settings.IMP.MESSAGES.STARTED_PROCESSING_MESSAGE
                                    .replaceAll("%checked", "-")
                                    .replaceAll("%to_check", "-"))))
                    .complete();
        } catch (Exception e) {
            return;
        }

        if (Settings.IMP.ROLES.USE_ROLE_LIMITS && (mode == BotMode.SIMULTANEOUS || mode == BotMode.GUILD) && message.isFromGuild()) {
            Map<String, Integer> roles = Settings.IMP.ROLES.ROLES;
            List<Role> memberRoles = message.getMember().getRoles();
            if (memberRoles.isEmpty() || OtherUtils.getMaxRoleValue(memberRoles, roles) == Integer.MIN_VALUE ||
                database.getCheckedPlugins(message.getMember().getUser()) >= OtherUtils.getMaxRoleValue(memberRoles, roles)) {
                sendErrorMessage(message, Settings.IMP.MESSAGES.PLUGIN_DAILY_LIMIT_MESSAGE, null);
                startedProcessing.delete().queue();
                return;
            }
        }

        List<CustomMessageEmbed> embeds = new ArrayList<>();
        List<Message.Attachment> attachments = attachmentList.stream()
                .filter(attachment -> attachment.getFileName().endsWith(".jar"))
                .toList();
        int checkedPlugins = 0;
        int totalPluginsToCheck = attachments.size();
        for (Message.Attachment attachment : attachments) {
            if (attachment.getFileName().endsWith(".jar") && attachment.getSize() > Settings.IMP.LIMITS.FILE_SIZE) {
                embeds.add(EmbedMessageBuilder.buildError(
                        Settings.IMP.MESSAGES.FILE_REACHED_SIZE_LIMIT
                                .replaceAll("%file_size_limit", OtherUtils.humanReadableByteCountBin(Settings.IMP.LIMITS.FILE_SIZE))
                                .replaceAll("%file_name", attachment.getFileName())
                                .replaceAll("%file_size", OtherUtils.humanReadableByteCountBin(attachment.getSize())), attachment));
                continue;
            }
            User author = message.getAuthor();
            long authorId = author.getIdLong();
            bannedUser = database.getBannedUser(authorId);
            if (bannedUser != null && !bannedUser.isExpired()) {
                sendBanMessage(message, bannedUser.expirationToSeconds());
                startedProcessing.delete().queue();
                return;
            }
            database.updateCheckedPlugins(author, 1);
            try {
                String fileName = FileUtils.getUniqueFileName(attachment);
                File attachmentFile = new File("tmp/" + fileName);
                database.addPlugin(authorId, attachment.getFileName());
                if (Settings.IMP.LIMITS.USE_AUTO_BANS) {
                    if (database.getPlugins(authorId).size() >= Settings.IMP.LIMITS.PLUGIN_LIMIT) {
                        banUserForSpammingPlugins(message, Settings.IMP.LIMITS.PLUGINS_SPAM_BAN_TIME);
                        startedProcessing.delete().queue();
                        return;
                    }
                    if (OtherUtils.countDuplicates(database.getPlugins(authorId)) >= Settings.IMP.LIMITS.DUPLICATE_LIMIT) {
                        banUserForSpammingPlugins(message, Settings.IMP.LIMITS.DUPLICATES_SPAM_BAN_TIME);
                        startedProcessing.delete().queue();
                        return;
                    }
                }
                FileUtils.downloadFile(attachment.getUrl(), attachmentFile.getAbsolutePath());
                try {
                    ScanResult result = scanner.analyze(attachmentFile);
                    database.updateLatestCheck(authorId);
                    logger.info("Checked {} from {}. {} classes, {} files, byte size {}",
                            attachment.getFileName(), author.getName(), scanner.getClasses().size(),
                            scanner.getFiles().size(), attachment.getSize());
                    checkedPlugins += 1;
                    startedProcessing.editMessage(MessageEditData.fromEmbeds(
                            EmbedMessageBuilder.buildNotify(Settings.IMP.MESSAGES.STARTED_PROCESSING_MESSAGE
                                    .replaceAll("%checked", String.valueOf(checkedPlugins))
                                    .replaceAll("%to_check", String.valueOf(totalPluginsToCheck))))).queue();
                    if (attachmentFile.delete()) {
                        EmbedMessageBuilder builder = new EmbedMessageBuilder();
                        embeds.add(builder.buildScanResult(attachment, result));
                    } else {
                        embeds.add(EmbedMessageBuilder.buildError(Settings.IMP.MESSAGES.DELETE_ERROR_MESSAGE, attachment));
                    }
                } catch (Exception e) {
                    if (e instanceof InvalidPluginException) {
                        embeds.add(EmbedMessageBuilder.buildError(Settings.IMP.MESSAGES.NOT_A_PLUGIN_ERROR_MESSAGE, attachment));
                    } else {
                        if (e.getLocalizedMessage().contains("ZipException")) {
                            embeds.add(EmbedMessageBuilder.buildError(Settings.IMP.MESSAGES.NOT_A_PLUGIN_ERROR_MESSAGE, attachment));
                        } else {
                            e.printStackTrace();
                            logger.error("Error while checking {} from {}: {}", attachment.getFileName(), author.getName(), e.getMessage());
                            embeds.add(EmbedMessageBuilder.buildError(Settings.IMP.MESSAGES.INTERNAL_ERROR_MESSAGE, attachment));
                        }
                    }
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        EmbedColorSorting.sort(embeds);
        List<MessageEmbed> embedList = embeds.stream().map(CustomMessageEmbed::getEmbed).toList();
        startedProcessing.editMessage(MessageEditData.fromEmbeds(embedList)).queue();
    }

    private void sendErrorMessage(Message message, String reason, Message.Attachment attachment) {
        message.getChannel().sendMessage(MessageCreateData.fromEmbeds(
                EmbedMessageBuilder.buildError(reason, attachment).getEmbed())
        ).queue();
    }

    private void sendBanMessage(Message message, BigInteger banTime) {
        sendErrorMessage(message, Settings.IMP.MESSAGES.BANNED_MESSAGE
                .replaceAll("%ban_reason", Settings.IMP.MESSAGES.SPAM_BLOCK_REASON)
                .replaceAll("%ban_expiration", OtherUtils.timeToString(banTime)), null);
    }

    private void banUserForSpammingPlugins(Message message, String banTime) {
        sendErrorMessage(message, Settings.IMP.MESSAGES.BANNED_MESSAGE
                .replaceAll("%ban_reason", Settings.IMP.MESSAGES.SPAM_BLOCK_REASON)
                .replaceAll("%ban_expiration", OtherUtils.timeToString(OtherUtils.convertTime(banTime))), null);
        database.addBanned(message.getAuthor().getIdLong(), Settings.IMP.MESSAGES.SPAM_BLOCK_REASON,
                OtherUtils.convertTime(banTime).longValue());
        database.clearPlugins(message.getAuthor().getIdLong());
    }
}
