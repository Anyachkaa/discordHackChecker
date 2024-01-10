package ru.itskekoff.hackchecker.framework.checks.impl.bukkit;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

public class BukkitParticleCrashCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (isLdcContains(node, "10000000 normal", "100000000 normal")) {
                return new ScanReport("Плагин отправляет много \"партиклов\", крашая клиент", Priority.HIGH);
            }
        }
        return null;
    }
}
