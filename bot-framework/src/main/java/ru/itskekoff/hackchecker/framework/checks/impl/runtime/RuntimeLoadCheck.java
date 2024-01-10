package ru.itskekoff.hackchecker.framework.checks.impl.runtime;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

public class RuntimeLoadCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (isMethodReferenced(node, "java/lang/Runtime", "load") ||
                    isMethodReferenced(node, "java/lang/Runtime", "loadLibrary") ||
                    isMethodReferenced(node, "java/lang/System", "load") ||
                    isMethodReferenced(node, "java/lang/System", "loadLibrary")) {
                return new ScanReport("Плагин может загрузить нативные библиотеки", Priority.MODERATE);
            }
        }
        return null;
    }
}
