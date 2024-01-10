package ru.itskekoff.hackchecker.framework.checks.impl.obfuscation;

import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.VarInsnNode;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class ParamorphismCheck extends Check {
    @Override
    public ScanReport process() {
        AtomicBoolean foundObfuscation = new AtomicBoolean(false);
        classes.values().stream().flatMap(classNode -> classNode.methods.stream()).forEach(methodNode -> {
            Arrays.stream(methodNode.instructions.toArray())
                    .filter(node -> node.getOpcode() == GOTO)
                    .map(JumpInsnNode.class::cast).forEach(node -> {
                        LabelNode labelNode = node.label;
                        if (labelNode.getNext().getOpcode() == GOTO &&
                            methodNode.instructions.indexOf(labelNode.getNext()) > methodNode.instructions.indexOf(node)) {
                            foundObfuscation.set(true);
                        } else if (labelNode.getNext() instanceof JumpInsnNode &&
                                   labelNode.getNext().getNext() instanceof VarInsnNode &&
                                   labelNode.getNext().getNext().getNext() instanceof JumpInsnNode) {
                            foundObfuscation.set(true);
                        }
                    });
        });
        if (classes.values().stream().anyMatch(classNode -> classNode.name.endsWith("PackedClassLoader") &&
                                                            classNode.superName.equals("java/lang/ClassLoader"))) {
            foundObfuscation.set(true);
        }
        if (foundObfuscation.get()) {
            return new ScanReport("Плагин содержит обфускацию Paramorphism", Priority.MODERATE);
        }
        return null;
    }
}
