package ru.itskekoff.hackchecker.framework.checks.impl.runtime;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;

public class ClassLoaderUsingCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (isClassReferenced(node, "java/lang/ClassLoader") ||
                    isClassReferenced(node, "java/lang/URLClassLoader") ||
                    isMethodReferenced(node, "java/lang/Thread", "getContextClassLoader")) {
                return new ScanReport("Плагин получает доступ к загрузчику классов", Priority.MODERATE);
            }
        }
        return null;
    }
}
