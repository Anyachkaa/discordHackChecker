package ru.itskekoff.hackchecker.framework.checks.impl.virus;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

public class PluginMetricsCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (isLdcReferenced(node, "plugins/PluginMetrics.jar")) {
                return new ScanReport("Плагин заражен вирусом PluginMetrics", Priority.CRITICAL);
            }
        }
        return null;
    }
}
