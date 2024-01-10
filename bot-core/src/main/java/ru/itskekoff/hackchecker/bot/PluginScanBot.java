package ru.itskekoff.hackchecker.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itskekoff.hackchecker.bot.configuration.Settings;
import ru.itskekoff.hackchecker.bot.database.DataManager;
import ru.itskekoff.hackchecker.bot.listeners.ChannelMessageListener;
import ru.itskekoff.hackchecker.bot.listeners.button.ButtonInteractionListener;
import ru.itskekoff.hackchecker.bot.listeners.slash.ChannelSlashCommandsListener;
import ru.itskekoff.hackchecker.bot.listeners.slash.UserManageSlashCommandsListener;
import ru.itskekoff.hackchecker.framework.scan.PluginScanner;

import java.io.File;

public class PluginScanBot {
    public static final Logger logger
            = LoggerFactory.getLogger(PluginScanBot.class);
    public static final DataManager database = new DataManager();
    public static PluginScanner scanner;
    public static JDA jda;

    public static void main(String[] args) {
        new PluginScanBot().run();
    }

    public void run() {
        File tmpDir = new File("tmp");
        if (!tmpDir.exists()) {
            if (tmpDir.mkdir()) {
                logger.info("Created temp directory");
            } else {
                logger.error("Can't create temp directory. Check permissions.");
                System.exit(-1);
            }
        }
        database.init();
        Settings.IMP.reload(new File("./config.yml"));
        if (Settings.IMP.MAIN.BOT_TOKEN.equals("token")) {
            logger.error("Enter token in config.yml");
            System.exit(-1);
        }
        scanner = new PluginScanner();
        logger.info("Registered {} checks", scanner.getManager().getChecks().size());
        logger.info("Starting bot...");
        JDABuilder builder = JDABuilder.create(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                .setToken(Settings.IMP.MAIN.BOT_TOKEN)
                .addEventListeners(
                        new UserManageSlashCommandsListener(),
                        new ChannelSlashCommandsListener(),
                        new ChannelMessageListener(),
                        new ButtonInteractionListener())
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .disableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER,
                        CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.SCHEDULED_EVENTS);
        jda = builder.build();
        jda.updateCommands().addCommands(
                Commands.slash("delete", Settings.IMP.MESSAGES.COMMANDS.DELETE.COMMAND_DESCRIPTION)
                        .addOption(OptionType.CHANNEL, "channel",
                                Settings.IMP.MESSAGES.COMMANDS.DELETE.CHANNEL_ARG_DESCRIPTION, false),
                Commands.slash("send", Settings.IMP.MESSAGES.COMMANDS.SEND.COMMAND_DESCRIPTION)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .addOption(OptionType.CHANNEL, "channel",
                                Settings.IMP.MESSAGES.COMMANDS.SEND.CHANNEL_ARG_DESCRIPTION, false),
                Commands.slash("ban", Settings.IMP.MESSAGES.COMMANDS.BAN.COMMAND_DESCRIPTION)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .addOption(OptionType.USER, "user", Settings.IMP.MESSAGES.COMMANDS.BAN.USER_ARG_DESCRIPTION, true)
                        .addOption(OptionType.STRING, "time", Settings.IMP.MESSAGES.COMMANDS.BAN.TIME_ARG_DESCRIPTION,
                                true)
                        .addOption(OptionType.STRING, "reason", Settings.IMP.MESSAGES.COMMANDS.BAN.REASON_ARG_DESCRIPTION, false),
                Commands.slash("unban", Settings.IMP.MESSAGES.COMMANDS.UNBAN.COMMAND_DESCRIPTION)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .addOption(OptionType.USER, "user", Settings.IMP.MESSAGES.COMMANDS.UNBAN.USER_ARG_DESCRIPTION, true)
        ).queue();
        logger.info("Bot started successfully!");
        database.createTasks();
        logger.info("Database tasks launched.");
    }
}