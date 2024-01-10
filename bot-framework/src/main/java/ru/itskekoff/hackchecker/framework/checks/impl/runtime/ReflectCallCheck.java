package ru.itskekoff.hackchecker.framework.checks.impl.runtime;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

public class ReflectCallCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (isMethodReferenced(node, "java/lang/reflect/Method", "invoke")) {
                return new ScanReport("Плагин использует рефлексию (может скрывать вредоносные вызовы)", Priority.LOW);
            }
        }
        return null;
    }
}
