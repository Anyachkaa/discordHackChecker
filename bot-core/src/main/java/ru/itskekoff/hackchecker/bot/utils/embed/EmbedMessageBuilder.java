package ru.itskekoff.hackchecker.bot.utils.embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import ru.itskekoff.hackchecker.bot.configuration.Settings;
import ru.itskekoff.hackchecker.bot.utils.OtherUtils;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.ScanReport;
import ru.itskekoff.hackchecker.framework.types.ScanResult;
import ru.itskekoff.hackchecker.bot.utils.ColorUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class EmbedMessageBuilder {
    private final Map<Priority, Color> priorityColorMap;

    public EmbedMessageBuilder() {
        priorityColorMap = new LinkedHashMap<>();
        priorityColorMap.put(Priority.CRITICAL, ColorUtils.getColor(Settings.IMP.COLORS.PRIORITY.CRITICAL));
        priorityColorMap.put(Priority.HIGH, ColorUtils.getColor(Settings.IMP.COLORS.PRIORITY.HIGH));
        priorityColorMap.put(Priority.MODERATE, ColorUtils.getColor(Settings.IMP.COLORS.PRIORITY.MODERATE));
        priorityColorMap.put(Priority.LOW, ColorUtils.getColor(Settings.IMP.COLORS.PRIORITY.LOW));
    }

    public CustomMessageEmbed buildScanResult(Message.Attachment attachment, ScanResult result) {
        List<ScanReport> reports = result.getReports();
        Optional<Priority> highestPriority = reports.stream()
                .map(ScanReport::getPriority)
                .min(Comparator.comparingInt(Enum::ordinal));
        Color color = highestPriority.map(priorityColorMap::get).orElse(null);
        if (reports.isEmpty()) {
            color = ColorUtils.getColor(Settings.IMP.COLORS.PRIORITY.EMPTY);
        }
        CustomMessageEmbed embed = new CustomMessageEmbed(new EmbedBuilder()
                .setTitle(formatMessage(Settings.IMP.MESSAGES.EMBEDS.CHECKED_FILE.FILE_CHECKED_HEADER_MESSAGE, attachment))
                .setDescription(reports.isEmpty() ? formatMessage(Settings.IMP.MESSAGES.EMBEDS.CHECKED_FILE.NOTHING_FOUND_MESSAGE, attachment) :
                        (reports.stream().anyMatch(report -> report.getPriority() == Priority.CRITICAL)
                                ? ((Settings.IMP.MESSAGES.EMBEDS.CHECKED_FILE.EMBED_CRITICAL_WARNING_ENABLED) ?
                                formatMessage(Settings.IMP.MESSAGES.EMBEDS.CHECKED_FILE.CRITICAL_WARNING_MESSAGE, attachment) : "") : "") +
                        reports.stream()
                                .map(report -> formatMessage(Settings.IMP.MESSAGES.EMBEDS.CHECKED_FILE.DETECT_FORMAT_MESSAGE, attachment)
                                        .replaceAll("%priority", report.getPriority().name())
                                        .replaceAll("%details", report.getMessage()))
                                .distinct()
                                .collect(Collectors.joining("\n")))
                .setFooter("Разработчик: itskekoff")
                .setColor(color).build());
        embed.addReports(reports);
        return embed;
    }

    public static CustomMessageEmbed buildError(String details, Message.Attachment attachment) {
        return new CustomMessageEmbed(new EmbedBuilder()
                .setTitle(attachment != null
                        ? formatMessage(Settings.IMP.MESSAGES.EMBEDS.ERROR.PROCESSING_ERROR_MESSAGE_HEADER, attachment)
                        .replaceAll("%error", details)
                        : formatMessage(Settings.IMP.MESSAGES.EMBEDS.ERROR.ERROR_MESSAGE_HEADER, null)
                        .replaceAll("%error", details))
                .setDescription(formatMessage(Settings.IMP.MESSAGES.EMBEDS.ERROR.ERROR_MESSAGE, attachment)
                        .replaceAll("%error", details))
                .setFooter("Разработчик: itskekoff")
                .setColor(ColorUtils.getColor(Settings.IMP.COLORS.ERROR_COLOR)).build());
    }

    public static MessageEmbed buildNotify(String details) {
        return new EmbedBuilder()
                .setTitle(Settings.IMP.MESSAGES.EMBEDS.NOTIFY.NOTIFY_MESSAGE_HEADER
                        .replaceAll("%details", details))
                .setDescription(Settings.IMP.MESSAGES.EMBEDS.NOTIFY.NOTIFY_MESSAGE
                        .replaceAll("%details", details))
                .setFooter("Разработчик: itskekoff")
                .setColor(ColorUtils.getColor(Settings.IMP.COLORS.NOTIFY_COLOR)).build();
    }

    public static String formatMessage(String message, Message.Attachment attachment) {
        if (attachment == null)
            return message;
        return message.replaceAll("%file_name", attachment.getFileName())
                .replaceAll("%file_size", OtherUtils.humanReadableByteCountBin(attachment.getSize()));
    }
}
