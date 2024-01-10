package ru.itskekoff.hackchecker.framework.checks.impl.web;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

public class FileUploaderCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (isMethodReferenced(node, "java/io/DataOutputStream", "write") &&
                    isClassReferenced(node, "BufferedOutputStream")) {
                return new ScanReport("Плагин может отправить данные на сайт/сервер", Priority.LOW);
            }
        }
        return null;
    }
}
