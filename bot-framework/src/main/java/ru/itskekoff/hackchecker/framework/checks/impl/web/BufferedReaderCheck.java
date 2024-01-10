package ru.itskekoff.hackchecker.framework.checks.impl.web;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

public class BufferedReaderCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (isMethodReferenced(node, "java/io/BufferedReader", "readLine")) {
                return new ScanReport("Плагин может считывать информацию с сайта/файла", Priority.LOW);
            }
        }
        return null;
    }
}
