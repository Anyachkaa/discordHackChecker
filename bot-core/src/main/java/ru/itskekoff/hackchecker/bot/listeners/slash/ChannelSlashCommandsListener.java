package ru.itskekoff.hackchecker.bot.listeners.slash;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import ru.itskekoff.hackchecker.bot.configuration.BotMode;
import ru.itskekoff.hackchecker.bot.configuration.Settings;
import ru.itskekoff.hackchecker.bot.utils.ColorUtils;
import ru.itskekoff.hackchecker.bot.utils.OtherUtils;
import ru.itskekoff.hackchecker.bot.utils.embed.EmbedMessageBuilder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.itskekoff.hackchecker.bot.PluginScanBot.database;

public class ChannelSlashCommandsListener extends ListenerAdapter {
    public static final String CHECK_SUFFIX = "-check";

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        BotMode mode = Settings.IMP.MAIN.BOT_MODE;
        switch (event.getName()) {
            case "send": {
                if (isNotAllowed(event, mode)) return;
                TextChannel targetChannel = getChannel(event);
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Проверка плагинов")
                        .setDescription(String.join("\n", Settings.IMP.MESSAGES.SENT_MESSAGE)
                                .replaceAll("%file_size_limit", OtherUtils.humanReadableByteCountBin(Settings.IMP.LIMITS.FILE_SIZE))
                                .replaceAll("%plugin_limit_reset", OtherUtils.timeToString(OtherUtils.convertTime(Settings.IMP.LIMITS.PLUGIN_RESET_TIME)))
                                .replaceAll("%plugins_ban_time", OtherUtils.timeToString(OtherUtils.convertTime(Settings.IMP.LIMITS.PLUGINS_SPAM_BAN_TIME)))
                                .replaceAll("%duplicates_ban_time", OtherUtils.timeToString(OtherUtils.convertTime(Settings.IMP.LIMITS.DUPLICATES_SPAM_BAN_TIME))))
                        .setFooter("Разработчик: itskekoff")
                        .setColor(ColorUtils.getColor(Settings.IMP.COLORS.MAIN_COLOR))
                        .build();
                targetChannel.sendMessage(MessageCreateData.fromEmbeds(embed))
                        .addActionRow(Button.success("create", "Создать канал"))
                        .queue();
                event.reply(Settings.IMP.MESSAGES.SUCCESS_SENT_MESSAGE)
                        .setEphemeral(true)
                        .queue();
                break;
            }
            case "delete": {
                if (isNotAllowed(event, mode)) return;
                TextChannel targetChannel = getChannel(event);
                Member member = event.getMember();
                if (targetChannel == null) {
                    throwError(Settings.IMP.MESSAGES.DELETE_NOT_ALLOWED_CHANNEL_MESSAGE, event);
                    return;
                }
                if (!isCheckChannel(targetChannel) && event.getOption("channel") == null) {
                    throwError(Settings.IMP.MESSAGES.DELETE_NOT_IN_CHANNEL_MESSAGE, event);
                    return;
                }
                if (event.getOption("channel") == null && isCheckChannel(targetChannel)) {
                    if (!isUserChannel(member, targetChannel)) {
                        if (member.hasPermission(Permission.ADMINISTRATOR)) {
                            processAdminDelete(event, targetChannel);
                            return;
                        }
                    }
                }
                if (event.getOption("channel") != null && member.hasPermission(Permission.ADMINISTRATOR)) {
                    if (!isCheckChannel(targetChannel)) {
                        throwError(Settings.IMP.MESSAGES.DELETE_NOT_ALLOWED_CHANNEL_MESSAGE, event);
                        return;
                    }
                    processAdminDelete(event, targetChannel);
                    return;
                }
                processDelete(member, targetChannel, event);
            }
        }
    }

    private static boolean isNotAllowed(@NotNull SlashCommandInteractionEvent event, BotMode mode) {
        return OtherUtils.isAllowedToUse(event, mode);
    }


    private boolean isCheckChannel(TextChannel channel) {
        return channel.getName().endsWith(CHECK_SUFFIX);
    }

    private boolean isUserChannel(Member member, TextChannel channel) {
        return channel.getName().split(CHECK_SUFFIX)[0].equals(OtherUtils.formatUser(member.getUser().getName()));
    }

    private void processAdminDelete(@NotNull SlashCommandInteractionEvent event, TextChannel targetChannel) {
        Optional<Member> optionalMember = getMember(targetChannel, event);
        if (optionalMember.isPresent() || database.isUserWithChannelExists(targetChannel.getIdLong())) {
            if (optionalMember.isEmpty()) {
                database.removeChannel(targetChannel);
                event.reply(MessageCreateData.fromEmbeds(
                                EmbedMessageBuilder.buildNotify(Settings.IMP.MESSAGES.CHANNEL_DELETED_MESSAGE)))
                        .setEphemeral(true)
                        .queue();
                return;
            }
            processDelete(optionalMember.get(), targetChannel, event);
        } else {
            throwError(Settings.IMP.MESSAGES.DELETE_NOT_ALLOWED_CHANNEL_MESSAGE, event);
        }
    }


    private void throwError(String reason, SlashCommandInteractionEvent event) {
        event.reply(MessageCreateData.fromEmbeds(EmbedMessageBuilder.buildError(reason, null).getEmbed()))
                .setEphemeral(true).queue();
    }

    private void processDelete(Member member, TextChannel channel, SlashCommandInteractionEvent event) {
        if (!isCheckChannel(channel)) {
            throwError(Settings.IMP.MESSAGES.DELETE_NOT_ALLOWED_CHANNEL_MESSAGE, event);
            return;
        }
        if (database.isUserAndChannelCorrect(member.getUser(), channel)) {
            database.removeChannel(channel, member.getUser().getIdLong());
            event.reply(MessageCreateData.fromEmbeds(
                            EmbedMessageBuilder.buildNotify(Settings.IMP.MESSAGES.CHANNEL_DELETED_MESSAGE)))
                    .setEphemeral(true)
                    .queue();
        } else {
            event.reply(MessageCreateData.fromEmbeds(
                            EmbedMessageBuilder.buildError(Settings.IMP.MESSAGES.DELETE_NOT_ALLOWED_CHANNEL_MESSAGE, null).getEmbed()))
                    .setEphemeral(true).queue();
        }
    }

    private TextChannel getChannel(SlashCommandInteractionEvent event) {
        if (event.getChannel() instanceof TextChannel) {
            TextChannel targetChannel = event.getChannel().asTextChannel();
            if (event.getOption("channel") != null) {
                Channel channel = Objects.requireNonNull(event.getOption("channel")).getAsChannel();
                if (channel instanceof TextChannel) {
                    return (TextChannel) channel;
                }
                return null;
            }
            return targetChannel;
        }
        return null;
    }

    private Optional<Member> getMember(TextChannel targetChannel, SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) {
            return Optional.empty();
        }
        List<Member> memberList = event.getGuild().getMembers();
        return memberList.stream()
                .filter(member ->
                        OtherUtils.formatUser(member.getUser().getName()).equals(targetChannel.getName().split(CHECK_SUFFIX)[0]))
                .findFirst();
    }
}
