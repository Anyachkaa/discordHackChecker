package ru.itskekoff.hackchecker.framework.checks.impl.virus;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

public class HostFlowCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (isLdcContains(node, "().a(getDataFolder().getParent());")) {
                return new ScanReport("Плагин заражен вирусом HostFlow", Priority.CRITICAL);
            }
        }
        return null;
    }
}
