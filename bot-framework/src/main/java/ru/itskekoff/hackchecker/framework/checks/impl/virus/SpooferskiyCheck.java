package ru.itskekoff.hackchecker.framework.checks.impl.virus;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

public class SpooferskiyCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (isLdcContains(node,
                    "org.bukkit.plugin.PluginManager:disablePlugin",
                    "java.io.File:delete",
                    "org.bukkit.entity.Player:setOp")) {
                return new ScanReport("Плагин может сильно навредить серверу (удалить)", Priority.CRITICAL);
            }
        }
        return null;
    }
}
