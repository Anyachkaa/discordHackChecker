package ru.itskekoff.hackchecker.bot.utils.embed;

import net.dv8tion.jda.api.entities.MessageEmbed;
import ru.itskekoff.hackchecker.bot.configuration.Settings;
import ru.itskekoff.hackchecker.bot.utils.ColorUtils;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

import java.awt.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmbedColorSorting {
    private static final Map<Color, Integer> colorPriorityMap;

    static {
        colorPriorityMap = new HashMap<>();
        colorPriorityMap.put(ColorUtils.getColor(Settings.IMP.COLORS.ERROR_COLOR), 6);
        colorPriorityMap.put(ColorUtils.getColor(Settings.IMP.COLORS.PRIORITY.CRITICAL), 5);
        colorPriorityMap.put(ColorUtils.getColor(Settings.IMP.COLORS.PRIORITY.HIGH), 4);
        colorPriorityMap.put(ColorUtils.getColor(Settings.IMP.COLORS.PRIORITY.MODERATE), 3);
        colorPriorityMap.put(ColorUtils.getColor(Settings.IMP.COLORS.PRIORITY.LOW), 2);
        colorPriorityMap.put(ColorUtils.getColor(Settings.IMP.COLORS.PRIORITY.EMPTY), 1);
    }

    public static void sort(List<CustomMessageEmbed> embeds) {
        // sort by priority
        Comparator<CustomMessageEmbed> comparator = Comparator
                .comparing((CustomMessageEmbed e) -> e.getReports().stream()
                                .collect(Collectors.groupingBy(ScanReport::getPriority, Collectors.counting())),
                        (Map<Priority, Long> m1, Map<Priority, Long> m2) -> {
                            for (Priority priority : Priority.values()) {
                                long count1 = m1.getOrDefault(priority, 0L);
                                long count2 = m2.getOrDefault(priority, 0L);
                                if (count1 != count2) {
                                    return Long.compare(count2, count1);
                                }
                            }
                            return 0;
                        });
        embeds.sort(comparator);

        // sort by color
        embeds.sort((e1, e2) -> {
            Color color1 = e1.getEmbed().getColor();
            Color color2 = e2.getEmbed().getColor();
            return colorPriorityMap.getOrDefault(color2, 1) - colorPriorityMap.getOrDefault(color1, 1);
        });
    }

}
