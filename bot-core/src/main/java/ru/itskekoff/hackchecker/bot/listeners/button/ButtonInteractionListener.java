package ru.itskekoff.hackchecker.bot.listeners.button;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import ru.itskekoff.hackchecker.bot.PluginScanBot;
import ru.itskekoff.hackchecker.bot.configuration.Settings;
import ru.itskekoff.hackchecker.bot.utils.ColorUtils;
import ru.itskekoff.hackchecker.bot.utils.OtherUtils;
import ru.itskekoff.hackchecker.bot.utils.embed.EmbedMessageBuilder;

import java.util.List;
import java.util.Map;

public class ButtonInteractionListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.isFromGuild() && event.getButton().getId() != null && event.getMember() != null) {
            if (event.getButton().getId().equals("create")) {
                Map<String, Integer> roles = Settings.IMP.ROLES.ROLES;
                List<Role> memberRoles = event.getMember().getRoles();
                int rolePluginLimit = OtherUtils.getMaxRoleValue(memberRoles, roles);
                if (!Settings.IMP.ROLES.USE_ROLE_LIMITS) {
                    rolePluginLimit = -1;
                }
                if (rolePluginLimit != Integer.MIN_VALUE) {
                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle("Дополнительная информация")
                            .setDescription(String.join("\n", Settings.IMP.MESSAGES.WELCOME_MESSAGE)
                                    .replaceAll("%file_size_limit", OtherUtils.humanReadableByteCountBin(Settings.IMP.LIMITS.FILE_SIZE))
                                    .replaceAll("%plugin_limit", String.valueOf(rolePluginLimit))
                                    .replaceAll("%plugin_reset_limit", OtherUtils.timeToString(OtherUtils.convertTime(Settings.IMP.LIMITS.PLUGIN_RESET_TIME)))
                                    .replaceAll("%plugins_ban_time", OtherUtils.timeToString(OtherUtils.convertTime(Settings.IMP.LIMITS.PLUGINS_SPAM_BAN_TIME)))
                                    .replaceAll("%duplicates_ban_time", OtherUtils.timeToString(OtherUtils.convertTime(Settings.IMP.LIMITS.DUPLICATES_SPAM_BAN_TIME)))
                                    .replaceAll("%plugins_per_time", String.valueOf(Settings.IMP.LIMITS.PLUGIN_LIMIT))
                                    .replaceAll("%duplicates_per_time", String.valueOf(Settings.IMP.LIMITS.DUPLICATE_LIMIT))
                                    .replaceAll("%user_global_name", event.getUser().getName())
                                    .replaceAll("%user_id", event.getUser().getId()))
                            .setFooter("Разработчик: itskekoff")
                            .setColor(ColorUtils.getColor(Settings.IMP.COLORS.CHANNEL_MESSAGE_COLOR))
                            .build();
                    if (PluginScanBot.database.createChannelForUser(event.getMember(), embed)) {
                        event.reply(MessageCreateData.fromEmbeds(
                                        EmbedMessageBuilder.buildNotify(Settings.IMP.MESSAGES.SUCCESS_CREATED_CHANNEL_MESSAGE)))
                                .setEphemeral(true)
                                .queue();
                    } else {
                        event.reply(MessageCreateData.fromEmbeds(
                                        EmbedMessageBuilder.buildError(Settings.IMP.MESSAGES.CHANNEL_ALREADY_CREATED_MESSAGE, null).getEmbed()))
                                .setEphemeral(true)
                                .queue();
                    }
                } else {
                    event.reply(MessageCreateData.fromEmbeds(
                                    EmbedMessageBuilder.buildError(Settings.IMP.MESSAGES.CHANNEL_NOT_ALLOWED_MESSAGE, null).getEmbed()))
                            .setEphemeral(true)
                            .queue();
                }
            }
        }
    }
}
