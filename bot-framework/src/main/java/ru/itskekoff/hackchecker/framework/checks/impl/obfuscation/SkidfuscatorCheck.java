package ru.itskekoff.hackchecker.framework.checks.impl.obfuscation;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

import java.util.Arrays;

public class SkidfuscatorCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode classNode : classes.values()) {
            if (classNode.methods.stream()
                    .flatMap(mn -> Arrays.stream(mn.instructions.toArray()))
                    .filter(node -> node.getOpcode() == INVOKESTATIC)
                    .map(MethodInsnNode.class::cast)
                    .filter(node -> node.desc.equals("(Ljava/lang/String;I)Ljava/lang/String;"))
                    .filter(node -> isInteger(node.getPrevious()))
                    .anyMatch(node -> isString(node.getPrevious().getPrevious()))) {
                return new ScanReport("Плагин содержит обфускацию Skidfuscator", Priority.MODERATE);
            }
        }
        return null;
    }
}
