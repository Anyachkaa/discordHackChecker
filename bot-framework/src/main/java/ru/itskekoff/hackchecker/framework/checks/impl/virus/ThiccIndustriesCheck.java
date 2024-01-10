package ru.itskekoff.hackchecker.framework.checks.impl.virus;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

public class ThiccIndustriesCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (isClassReferenced(node, "com/thiccindustries/debugger/Debugger")) {
                return new ScanReport("Плагин содержит инжект от ThiccIndustries", Priority.CRITICAL);
            }
        }
        return null;
    }
}
