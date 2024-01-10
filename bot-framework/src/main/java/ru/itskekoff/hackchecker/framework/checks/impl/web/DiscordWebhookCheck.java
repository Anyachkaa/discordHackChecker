package ru.itskekoff.hackchecker.framework.checks.impl.web;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;
import ru.itskekoff.hackchecker.framework.utils.ClassUtils;

public class DiscordWebhookCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (isLdcContains(node, "https://discord.com/api/webhooks/")) {
                return new ScanReport("Плагин содержит дискорд вебхук", Priority.MODERATE);
            }
        }
        return null;
    }
}
