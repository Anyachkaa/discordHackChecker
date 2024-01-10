package ru.itskekoff.hackchecker.bot.configuration;

import net.elytrium.commons.config.YamlConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Settings extends YamlConfig {
    @Ignore
    public static final Settings IMP = new Settings();

    @Create
    public MAIN MAIN;

    @Comment("Main bot settings")
    public static class MAIN {
        @Comment("Bot token")
        public String BOT_TOKEN = "token";
        @Comment("Bot mode")
        @Comment("GUILD - discord server (all bot functionality available)")
        @Comment("DIRECT - personal messages (all commands blocked, only limits: plugins, duplicates, size")
        @Comment("SIMULTANEOUS - combination of the first two modes")
        public BotMode BOT_MODE = BotMode.SIMULTANEOUS;

        @Comment("Category where the verification channels will be")
        public long CATEGORY_ID = 10000000000000L;

        @Comment("Time in minutes after which the channel will be deleted (after latest check)")
        public int DELETE_TIME = (24 * 60) + (12 + 60);
    }

    @Create
    public Roles ROLES;

    @Comment("Role settings")
    public static class Roles {
        @Comment("Whether to use plugin limits by roles")
        public boolean USE_ROLE_LIMITS = true;

        @Comment("Roles and plugin limits per day")
        public Map<String, Integer> ROLES = Map.ofEntries(
                Map.entry("100000000000000", 15),
                Map.entry("200000000000000", 50));
    }

    @Create
    public Limits LIMITS;

    @Comment("Setting limits and blocks")
    @Comment("Available time formats: s, m, h, d, y")
    @Comment("For example:")
    @Comment("1m1s - 1 minute 1 second")
    @Comment("1y1m40s - 1 year, 1 minute, 40 seconds")
    public static class Limits {
        @Comment("Enable auto ban (spam, etc.)")
        public boolean USE_AUTO_BANS = true;

        @Comment("Maximum possible number of plugins for the specified time limit")
        public int PLUGIN_LIMIT = 12;

        @Comment("Maximum possible number of duplicates for the specified time limit")
        public int DUPLICATE_LIMIT = 3;

        @Comment("Specified time for the plugin count limit")
        public String PLUGINS_LIMIT_RESET_TIME = "1m";

        @Comment("Specified time for the duplicate count limit")
        public String DUPLICATES_LIMIT_RESET_TIME = "2m";

        @Comment("Automatic plugin counter reset time")
        public String PLUGIN_RESET_TIME = "1d";

        @Comment("Time to ban a user for plugin spam")
        public String PLUGINS_SPAM_BAN_TIME = "1h";

        @Comment("Time to ban a user for duplicate spam")
        public String DUPLICATES_SPAM_BAN_TIME = "3h";

        @Comment("Maximum plugin size in binary bytes (1kb = 1024 bytes) (default - 30mb)")
        public int FILE_SIZE = (1024 * 1024) * 30;
    }

    @Create
    public Colors COLORS;

    @Comment("Setting colors for all messages")
    @Comment("All colors are specified in HEX (example: #ffccee)")
    @Comment("If you don't need a color, write none or leave the field blank")
    public static class Colors {
        @Comment("Main message color")
        public String MAIN_COLOR = "#add8e6";
        @Comment("Color of the message sent to the channel")
        public String CHANNEL_MESSAGE_COLOR = "#add8e6";
        @Comment("Information message color")
        public String NOTIFY_COLOR = "#5e6ad9";
        @Comment("Error color")
        public String ERROR_COLOR = "#bb0a1e";
        @Create
        public Priority PRIORITY;

        @Comment("Successful check message colors")
        @Comment("If nothing is found, the color will be equal to LOW priority.")
        public static class Priority {
            @Comment("CRITICAL priority color")
            public String CRITICAL = "#ff0000";
            @Comment("HIGH priority color")
            public String HIGH = "#ff5400";
            @Comment("MODERATE priority color")
            public String MODERATE = "#ffd600";
            @Comment("LOW priority color")
            public String LOW = "#9bff00";

            @Comment("Result color without detects")
            public String EMPTY = "#add8e6";
        }
    }


    @Create
    public Messages MESSAGES;

    @Comment("Settings for messages. Use \\n to create a new line.")
    public static class Messages {
        @Create
        public Commands COMMANDS;

        @Comment("Descriptions for all bot commands")
        public static class Commands {
            @Create
            public Delete DELETE;

            @Comment("Description and arguments for the \"delete\" slash command")
            public static class Delete {
                @Comment("Description")
                public String COMMAND_DESCRIPTION = "Удалить канал для проверок на хаки";

                @Comment("Description for the \"channel\" argument")
                public String CHANNEL_ARG_DESCRIPTION = "Канал, который будет удален";
            }

            @Create
            public Send SEND;

            @Comment("Description and arguments for the \"send\" slash command")
            public static class Send {
                @Comment("Description")
                public String COMMAND_DESCRIPTION = "Отправить сообщение с кнопкой  в канал";

                @Comment("Description for the \"channel\" argument")
                public String CHANNEL_ARG_DESCRIPTION = "Канал, куда будет отправлено сообщение";
            }

            @Create
            public Ban BAN;

            @Comment("Description and arguments for the \"ban\" slash command")
            public static class Ban {
                @Comment("Description")
                public String COMMAND_DESCRIPTION = "Забанить пользователя в боте";

                @Comment("Description for the \"user\" argument")
                public String USER_ARG_DESCRIPTION = "Пользователь";

                @Comment("Description for the \"time\" argument")
                public String TIME_ARG_DESCRIPTION = "Время (в формате 1d4s (s, m, h, d, y)";

                @Comment("Description for the \"reason\" argument")
                public String REASON_ARG_DESCRIPTION = "Причина блокировки";
            }

            @Create
            public Unban UNBAN;

            @Comment("Description and arguments for the \"unban\" slash command")
            public static class Unban {
                @Comment("Description")
                public String COMMAND_DESCRIPTION = "Разбанить пользователя в боте";

                @Comment("Description for the \"user\" argument")
                public String USER_ARG_DESCRIPTION = "Пользователь";
            }
        }

        @Create
        public Embeds EMBEDS;

        @Comment("Details/headings of messages")
        public static class Embeds {
            @Create
            public Checked CHECKED_FILE;

            @Comment("Values for each embed of a checked file")
            @Comment("In any string value, you can use the following parameters:")
            @Comment("%file_name - File name")
            @Comment("%file_size - Size of the sent file (formatted in units)")
            public static class Checked {
                @Comment("Shows a recommendation to delete the plugin if there is at least one CRITICAL detect")
                public boolean EMBED_CRITICAL_WARNING_ENABLED = true;

                @Comment("Header of successful file check")
                public String FILE_CHECKED_HEADER_MESSAGE = "Объект `%file_name` проверен";

                @Comment("Embed description value when no detects are found")
                public String NOTHING_FOUND_MESSAGE = "Ничего не было обнаружено.";

                @Comment("Recommendation message for CRITICAL detect")
                public String CRITICAL_WARNING_MESSAGE = "\n**В плагине обнаружены вирусы, вы должны его удалить.**\n\n";

                @Comment("Format of each detect. By default, everything will be like this: [PRIORITY] DESCRIPTION")
                @Comment("Additional parameters:")
                @Comment("%priority - Priority (for example - CRITICAL)")
                @Comment("%details - Detect description (for example - The plugin is infected with a virus ...)")
                public String DETECT_FORMAT_MESSAGE = "[%priority] %details";
            }

            @Create
            public Error ERROR;

            @Comment("Values for each error embed")
            @Comment("Each string value has the following parameters:")
            @Comment("%file_name - File name")
            @Comment("%file_size - Size of the sent file (formatted in units)")
            @Comment("%error - The error itself")
            public static class Error {
                @Comment("Header of error message when processing a file")
                @Comment("Parameters:")
                @Comment("%file_name - File name")
                @Comment("%file_size - Size of the sent file (formatted in units)")
                public String PROCESSING_ERROR_MESSAGE_HEADER = "Произошла ошибка в обработке `%file_name`";
                @Comment("Error not when processing a file")
                public String ERROR_MESSAGE_HEADER = "Произошла внутренняя ошибка";
                @Comment("Just an error")
                public String ERROR_MESSAGE = "`[%error]`";
            }

            @Create
            public Notify NOTIFY;

            @Comment("Informational embed.")
            @Comment("Has one parameter for each value:")
            @Comment("%details - The \"Information\" itself")
            public static class Notify {
                @Comment("Message header")
                public String NOTIFY_MESSAGE_HEADER = "Информация";

                @Comment("The information itself")
                public String NOTIFY_MESSAGE = "`%details`";
            }
        }

        @Comment("Message that has a channel creation button")
        @Comment("Parameters:")
        @Comment("%file_size_limit - File size limit in bytes, displayed in mebibytes and similar units")
        @Comment("%plugin_limit_reset - Time at which the plugin counter is reset")
        @Comment("%plugins_ban_time - Time for which a person will be banned for plugin limit")
        @Comment("%duplicates_ban_time - Time for which a person will be banned for duplicate limit")
        public List<String> SENT_MESSAGE = Arrays.asList(
                "Данный бот может проверить плагины на наличие вредоноса",
                "Чтобы проверить плагин, вам нужно нажать кнопку снизу",
                "Бот упомянет тебя в созданном канале и даст дальнейшие инструкции",
                "Создать канал можно только 1 раз до его удаления",
                "",
                "Определённая информация может быть ложной или не точной",
                "Финальный вердикт только на вашей стороне, бот лишь упрощает проверку");

        @Comment("Message that is sent to the channel when the user creates it")
        @Comment("Parameters:")
        @Comment("%file_size_limit - File size limit in bytes, displayed in mebibytes and similar units")
        @Comment("For example: \"The size limit is set to: `30 MiB`\"")
        @Comment("%plugin_limit - Plugin limit by roles")
        @Comment("%plugin_reset_limit - Time at which the plugin counter is reset")
        @Comment("%plugins_ban_time - Time for which a person will be banned for plugin limit")
        @Comment("%duplicates_ban_time - Time for which a person will be banned for duplicate limit")
        @Comment("For example: \"You will be banned for `1 minute 3 seconds`\"")
        @Comment("%plugins_per_time - Number of plugins that can be sent for the specified time (plugin-limit-time)")
        @Comment("%duplicates_per_time - Number of possible duplicates for the specified time (duplicate-limit-time)")
        @Comment("%user_global_name - Name of the user who created the channel (not a server member)")
        @Comment("%user_id - ID of the user who created the channel")
        public List<String> WELCOME_MESSAGE = Arrays.asList(
                "Данный бот принимает плагины и проверяет их на вредоносный код",
                "Чтобы проверить плагины, достаточно отправить их в чат",
                "Напоминаем, что бот лишь дает информацию о том, что может сделать плагин.",
                "Точный вердикт остается на Вашей стороне.",
                "",
                "На проверку каждого плагина установлен лимит по размеру в `%file_size_limit`",
                "Ваш лимит плагинов на момент создания канала: `%plugin_limit`",
                "Лимит сбрасывается каждый: `%plugin_reset_limit`",
                "",
                "При более `%plugins_per_time` плагинов за минуту, вас забанят на `%plugins_ban_time`",
                "При более `%duplicates_per_time` дупликатов за две минуты, вас забанят на `%duplicates_ban_time`",
                "При детекте уровня `CRITICAL`, вам следует удалить плагин с вашего сервера",
                "",
                "Для удаления канала, используйте slash команду `/delete`");

        @Comment("Message about the user reaching the daily plugin limit")
        public String PLUGIN_DAILY_LIMIT_MESSAGE = "У вас достигнут допустимый лимит по плагинам в сутки";

        @Comment("Message that the file is not a plugin")
        public String NOT_A_PLUGIN_ERROR_MESSAGE = "Файл не является плагином, если это ошибка, напишите разработчику";

        @Comment("Message that the file has exceeded the size limit")
        @Comment("Parameters:")
        @Comment("%file_size_limit - File size limit in bytes, displayed in mebibytes and similar units")
        @Comment("%file_name - File name")
        @Comment("%file_size - Size of the sent file (formatted in units)")
        public String FILE_REACHED_SIZE_LIMIT = "Файл `%file_name` превышает допустимый размер для файла.\\n" +
                                                "Максимальный размер: `%file_size_limit%`, а файл имеет размер: `%file_size`";

        @Comment("Message that you cannot perform this or that action due to the bot mode")
        public String NOT_ALLOWED_ACTION_MESSAGE = "Вы не можете выполнить это действие из-за текущего режима бота";

        @Comment("Error message when deleting a file")
        public String DELETE_ERROR_MESSAGE = "Произошла ошибка при удалении файла, напишите разработчику";

        @Comment("Message that it is impossible to delete a channel (when not performed in a check-channel)")
        public String DELETE_NOT_IN_CHANNEL_MESSAGE = "Вы не можете удалить канал не находясь в нём";

        @Comment("Message that it is impossible to delete a channel if it is not a check channel")
        public String DELETE_NOT_ALLOWED_CHANNEL_MESSAGE = "Вы не можете удалить канал, который не является каналом для проверок или не принадлежит юзеру бота";

        @Comment("Message that the check has started")
        @Comment("Parameters:")
        @Comment("%checked - means the current number of checked files")
        @Comment("%to_check - means the total number of files to check")
        public String STARTED_PROCESSING_MESSAGE = "Начата обработка файлов [%checked/%to_check]";

        @Comment("Message about successful channel creation")
        public String SUCCESS_CREATED_CHANNEL_MESSAGE = "Канал успешно создан.";

        @Comment("Message that the channel has already been created")
        public String CHANNEL_ALREADY_CREATED_MESSAGE = "У вас уже создан канал для проверок";

        @Comment("Message that the channel cannot be created")
        public String CHANNEL_NOT_ALLOWED_MESSAGE = "У вас нету прав на создание канала";

        @Comment("Channel deletion message")
        public String CHANNEL_DELETED_MESSAGE = "Канал удалён.";

        @Comment("Message about successful user ban")
        public String SUCCESS_BANNED_MESSAGE = "Пользователь успешно забанен.";

        @Comment("Message that the user is a bot")
        public String USER_IS_BOT_ERROR_MESSAGE = "Данный пользователь является ботом";

        @Comment("Message about successful user unban")
        public String SUCCESS_UNBANNED_MESSAGE = "Пользователь успешно разбанен.";

        @Comment("Message that the user is not banned")
        public String USER_IS_NOT_BANNED_ERROR_MESSAGE = "Данный пользователь не забанен.";

        @Comment("Message about successful sending of another message")
        public String SUCCESS_SENT_MESSAGE = "Сообщение отправлено.";

        @Comment("Message that the user is banned in the bot")
        @Comment("Parameters:")
        @Comment("%ban_reason - Reason")
        @Comment("%ban_expiration - Time after which the user will be unbanned")
        @Comment("For example: \"You are blocked.... for the reason: stupid. End date: 92.23m years")
        public String BANNED_MESSAGE = "Вы были заблокированы с причиной: %ban_reason. \nВам осталось сидеть в блокировке: %ban_expiration";

        @Comment("Message about the absence of a ban reason")
        public String NO_BAN_REASON_MESSAGE = "Причина не указана";

        @Comment("Reason for blocking for spamming plugins (reaching the plugin limit per minute or many duplicates)")
        public String SPAM_BLOCK_REASON = "Спам плагинами";

        @Comment("Internal error message")
        public String INTERNAL_ERROR_MESSAGE = "Произошла внутренняя ошибка. Лог отправлен разработчику";
    }

    @Create
    public Units UNITS;

    @Comment("All units")
    public static class Units {
        @Comment("Number units. These are abbreviations for numerical values")
        public List<String> NUMBER_UNITS = Arrays.asList("", "тыс", "млн", "млрд", "трлн");

        @Comment("Time labels. These are singular forms of time units")
        public List<String> TIME_LABELS = Arrays.asList("год", "день", "час", "минута", "секунда");

        @Comment("Few labels. These are plural forms of time units used with 2-4")
        public List<String> FEW_LABELS = Arrays.asList("года", "дня", "часа", "минуты", "секунды");

        @Comment("Many labels. These are plural forms of time units used with 5 and more")
        public List<String> MANY_LABELS = Arrays.asList("лет", "дней", "часов", "минут", "секунд");
    }
}


