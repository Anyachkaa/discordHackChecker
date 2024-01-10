package ru.itskekoff.hackchecker.framework.checks.impl.runtime;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

public class RuntimeExecuteCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (isMethodReferenced(node, "java/lang/Runtime", "exec") &&
                    isMethodReferenced(node, "java/lang/ProcessBuilder", "start")) {
                return new ScanReport("Плагин может выполнять системные команды", Priority.HIGH);
            }
        }
        return null;
    }
}
