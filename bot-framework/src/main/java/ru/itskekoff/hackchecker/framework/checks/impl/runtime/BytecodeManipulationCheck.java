package ru.itskekoff.hackchecker.framework.checks.impl.runtime;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

import java.util.Arrays;
import java.util.List;

public class BytecodeManipulationCheck extends Check {
    private final List<String> libs = Arrays.asList(
            "net/bytebuddy",
            "org/objectweb/asm",
            "javassist");

    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            for (String lib : libs) {
                if (isClassReferenced(node, lib)) {
                    return new ScanReport("Плагин может редактировать байткод других файлов", Priority.MODERATE);
                }
            }
        }
        return null;
    }

}
