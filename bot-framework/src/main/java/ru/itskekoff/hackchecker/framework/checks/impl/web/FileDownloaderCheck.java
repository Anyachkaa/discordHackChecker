package ru.itskekoff.hackchecker.framework.checks.impl.web;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

public class FileDownloaderCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (isMethodReferenced(node, "java/io/BufferedInputStream", "read") &&
                    isMethodReferenced(node, "java/io/FileOutputStream", "write")) {
                return new ScanReport("Плагин может скачать файл", Priority.LOW);
            }
        }
        return null;
    }
}
