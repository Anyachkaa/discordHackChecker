package ru.itskekoff.hackchecker.bot.listeners.slash;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import ru.itskekoff.hackchecker.bot.PluginScanBot;
import ru.itskekoff.hackchecker.bot.configuration.BotMode;
import ru.itskekoff.hackchecker.bot.configuration.Settings;
import ru.itskekoff.hackchecker.bot.utils.OtherUtils;
import ru.itskekoff.hackchecker.bot.utils.embed.EmbedMessageBuilder;
import ru.itskekoff.hackchecker.bot.utils.types.BannedUser;

public class UserManageSlashCommandsListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        BotMode mode = Settings.IMP.MAIN.BOT_MODE;
        switch (event.getName()) {
            case "ban": {
                if (isNotAllowed(event, mode)) break;
                Member target = event.getOption("user", OptionMapping::getAsMember);
                String reason = Settings.IMP.MESSAGES.NO_BAN_REASON_MESSAGE;
                if (event.getOption("reason") != null) {
                    reason = event.getOption("reason", OptionMapping::getAsString);
                }
                long time = OtherUtils.convertTime(event.getOption("time", OptionMapping::getAsString)).longValue();
                if (target != null) {
                    if (target.getUser().isBot()) {
                        event.reply(MessageCreateData.fromEmbeds(EmbedMessageBuilder.buildError(Settings.IMP.MESSAGES.USER_IS_BOT_ERROR_MESSAGE,
                                        null).getEmbed()))
                                .queue();
                        break;
                    }
                    PluginScanBot.database.addBanned(target.getIdLong(), reason, time);
                    event.reply(MessageCreateData.fromEmbeds(
                            EmbedMessageBuilder.buildNotify(Settings.IMP.MESSAGES.SUCCESS_BANNED_MESSAGE))).queue();
                }

                break;
            }
            case "unban": {
                if (isNotAllowed(event, mode)) break;
                Member target = event.getOption("user", OptionMapping::getAsMember);
                if (target != null) {
                    if (target.getUser().isBot()) {
                        event.reply(MessageCreateData.fromEmbeds(EmbedMessageBuilder.buildError(Settings.IMP.MESSAGES.USER_IS_BOT_ERROR_MESSAGE,
                                        null).getEmbed()))
                                .queue();
                        break;
                    }
                    BannedUser bannedUser = PluginScanBot.database.getBannedUser(target.getIdLong());
                    if (bannedUser == null || bannedUser.isExpired()) {
                        event.reply(MessageCreateData.fromEmbeds(EmbedMessageBuilder.buildError(Settings.IMP.MESSAGES.USER_IS_NOT_BANNED_ERROR_MESSAGE,
                                        null).getEmbed()))
                                .queue();
                        break;
                    }
                    PluginScanBot.database.removeBanned(target.getIdLong());
                    event.reply(MessageCreateData.fromEmbeds(
                            EmbedMessageBuilder.buildNotify(Settings.IMP.MESSAGES.SUCCESS_UNBANNED_MESSAGE))
                    ).queue();
                }
            }
        }
    }

    private boolean isNotAllowed(@NotNull SlashCommandInteractionEvent event, BotMode mode) {
        return OtherUtils.isAllowedToUse(event, mode);
    }
}
