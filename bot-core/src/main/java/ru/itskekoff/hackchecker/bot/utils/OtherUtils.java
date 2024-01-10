package ru.itskekoff.hackchecker.bot.utils;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import ru.itskekoff.hackchecker.bot.configuration.BotMode;
import ru.itskekoff.hackchecker.bot.configuration.Settings;
import ru.itskekoff.hackchecker.bot.utils.embed.EmbedMessageBuilder;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.CharacterIterator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.StringCharacterIterator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OtherUtils {

    public static <T> int countDuplicates(List<T> list) {
        Set<T> set = new HashSet<>();
        int duplicateCount = 0;
        for (T item : list) {
            if (!set.add(item)) {
                duplicateCount++;
            }
        }
        return duplicateCount;
    }

    public static String timeToString(BigInteger seconds) {
        String[] units = Settings.IMP.UNITS.NUMBER_UNITS.toArray(String[]::new);
        BigInteger[] timeUnits = {BigInteger.valueOf(31536000), BigInteger.valueOf(86400), BigInteger.valueOf(3600),
                BigInteger.valueOf(60), BigInteger.ONE};
        String[] timeLabels = Settings.IMP.UNITS.TIME_LABELS.toArray(String[]::new);
        String[] fewLabels = Settings.IMP.UNITS.FEW_LABELS.toArray(String[]::new);
        String[] manyLabels = Settings.IMP.UNITS.MANY_LABELS.toArray(String[]::new);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < timeUnits.length; i++) {
            BigInteger[] values = seconds.divideAndRemainder(timeUnits[i]);
            seconds = values[1];
            if (values[0].compareTo(BigInteger.ZERO) > 0) {
                String formattedNumber = formatLargeNumber(values[0], units);
                sb.append(formattedNumber).append(" ").append(pluralize(values[0], timeLabels[i], fewLabels[i], manyLabels[i])).append(", ");
            }
        }
        return sb.toString().replaceAll(", $", "");
    }

    public static String formatLargeNumber(BigInteger number, String[] units) {
        int unitIndex = 0;
        BigDecimal decimalNumber = new BigDecimal(number);
        while (decimalNumber.compareTo(BigDecimal.valueOf(1000)) >= 0 && unitIndex < units.length - 1) {
            decimalNumber = decimalNumber.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
            unitIndex++;
        }
        DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
        return df.format(decimalNumber).trim() + (units[unitIndex].isEmpty() ? "" : " " + units[unitIndex]);
    }

    public static String pluralize(BigInteger count, String singular, String few, String many) {
        int mod100 = count.mod(BigInteger.valueOf(100)).intValue();
        int mod10 = mod100 % 10;
        if (count.compareTo(BigInteger.valueOf(1000)) >= 0) {
            return many;
        } else {
            return switch (mod100) {
                case 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 -> many;
                default -> switch (mod10) {
                    case 1 -> singular;
                    case 2, 3, 4 -> few;
                    default -> many;
                };
            };
        }
    }

    public static BigInteger convertTime(String inputString) {
        Pattern pattern = Pattern.compile("(\\d+)([dhmsy])");
        Matcher matcher = pattern.matcher(inputString);
        BigInteger seconds = BigInteger.ZERO;
        while (matcher.find()) {
            BigInteger value = new BigInteger(matcher.group(1));
            String unit = matcher.group(2);
            switch (unit) {
                case "y" -> seconds = seconds.add(value.multiply(BigInteger.valueOf(31536000)));
                case "d" -> seconds = seconds.add(value.multiply(BigInteger.valueOf(86400)));
                case "h" -> seconds = seconds.add(value.multiply(BigInteger.valueOf(3600)));
                case "m" -> seconds = seconds.add(value.multiply(BigInteger.valueOf(60)));
                case "s" -> seconds = seconds.add(value);
            }
        }
        return seconds;
    }


    public static boolean isAllowedToUse(@NotNull SlashCommandInteractionEvent event, BotMode mode) {
        if (!event.isFromGuild() && (mode == BotMode.GUILD || mode == BotMode.SIMULTANEOUS)) {
            event.reply(MessageCreateData.fromEmbeds(
                            EmbedMessageBuilder.buildError(Settings.IMP.MESSAGES.NOT_ALLOWED_ACTION_MESSAGE, null).getEmbed()))
                    .setEphemeral(true)
                    .queue();
            return true;
        }
        return false;
    }


    public static String formatUser(String name) {
        return name.replaceAll("\\.", "");
    }

    public static String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }

    public static int getMaxRoleValue(List<Role> memberRoles, Map<String, Integer> roles) {
        int maxRoleValue = Integer.MIN_VALUE;
        for (Role role : memberRoles) {
            long roleId = role.getIdLong();
            Integer roleValue = roles.get(String.valueOf(roleId));
            if (roleValue != null && roleValue > maxRoleValue) {
                maxRoleValue = roleValue;
            }
        }
        return maxRoleValue;
    }
}

