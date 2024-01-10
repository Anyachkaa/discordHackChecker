package ru.itskekoff.hackchecker.framework.checks.impl.obfuscation;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

public class BinsecureCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (node.fields.stream().anyMatch(fieldNode -> fieldNode.desc.endsWith("[[[[[[[I"))) {
                return new ScanReport("Плагин содержит обфускацию BinSecure", Priority.MODERATE);
            }
        }
        return null;
    }
}
