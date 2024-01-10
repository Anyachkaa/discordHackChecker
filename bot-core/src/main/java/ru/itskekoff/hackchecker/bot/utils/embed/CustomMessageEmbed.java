package ru.itskekoff.hackchecker.bot.utils.embed;

import lombok.Getter;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CustomMessageEmbed {
    public List<ScanReport> reports = new ArrayList<>();
    public MessageEmbed embed;

    public CustomMessageEmbed(MessageEmbed embed) {
        this.embed = embed;
    }

    public void addReports(List<ScanReport> reports) {
        this.reports.addAll(reports);
    }
}
