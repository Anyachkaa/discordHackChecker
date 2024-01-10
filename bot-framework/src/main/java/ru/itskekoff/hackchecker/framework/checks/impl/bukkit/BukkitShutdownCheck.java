package ru.itskekoff.hackchecker.framework.checks.impl.bukkit;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

public class BukkitShutdownCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (isMethodReferenced(node, "org/bukkit/Bukkit", "shutdown")) {
                return new ScanReport("Плагин может выключить сервер", Priority.HIGH);
            }
        }
        return null;
    }
}
