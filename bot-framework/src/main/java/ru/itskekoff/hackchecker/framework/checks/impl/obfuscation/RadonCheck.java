package ru.itskekoff.hackchecker.framework.checks.impl.obfuscation;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class RadonCheck extends Check {
    private static final String BOOTSTRAP_DESC = "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
    private String owner;

    @Override
    public ScanReport process() {
        if (owner != null)
            owner = null;
        AtomicBoolean foundObfuscation = new AtomicBoolean(false);

        classes.values().forEach(classNode -> classNode.methods.forEach(methodNode -> Arrays.stream(methodNode.instructions.toArray())
                .filter(node -> node instanceof MethodInsnNode)
                .filter(node -> node.getOpcode() == INVOKESTATIC)
                .map(MethodInsnNode.class::cast)
                .filter(node -> owner == null || node.owner.equals(owner))
                .filter(node -> node.desc.equals("(Ljava/lang/Object;I)Ljava/lang/String;"))
                .filter(node -> isString(node.getPrevious().getPrevious()))
                .filter(node -> isInteger(node.getPrevious()))
                .forEach(node -> {
                    if (owner == null) {
                        init(node);
                    }
                })));
        if (owner != null) {
            foundObfuscation.set(true);
        }
        classes.values().stream().flatMap(classNode -> classNode.methods.stream())
                .flatMap(methodNode -> Arrays.stream(methodNode.instructions.toArray()))
                .filter(node -> node instanceof InvokeDynamicInsnNode)
                .filter(node -> isLong(node.getPrevious()))
                .filter(node -> isString(node.getPrevious().getPrevious()))
                .map(InvokeDynamicInsnNode.class::cast)
                .filter(node -> node.bsm.getDesc().equals(BOOTSTRAP_DESC))
                .forEach(node -> {
                    String owner = getString(node.getPrevious().getPrevious());
                    if (owner != null) {
                        foundObfuscation.set(true);
                    }
                });
        if (foundObfuscation.get()) {
            return new ScanReport("Плагин содержит обфускацию Radon", Priority.MODERATE);
        }
        return null;
    }

    private void init(MethodInsnNode methodInsnNode) {
        ClassNode classNode = classes.get(methodInsnNode.owner);
        if (isDecryptMethod(classNode, methodInsnNode)) {
            owner = methodInsnNode.owner;
        }
    }

    private boolean isDecryptMethod(ClassNode classNode, MethodInsnNode methodInsnNode) {
        return Arrays.stream(findMethod(classNode, methodInsnNode).orElseThrow().instructions.toArray())
                       .filter(node -> node instanceof MethodInsnNode)
                       .filter(node -> node.getOpcode() == INVOKEVIRTUAL)
                       .map(MethodInsnNode.class::cast)
                       .filter(node -> node.owner.equals("java/lang/String"))
                       .filter(node -> node.name.equals("hashCode"))
                       .filter(node -> node.desc.equals("()I"))
                       .filter(node -> isString(node.getPrevious()))
                       .filter(node -> node.getNext().getOpcode() == ISTORE)
                       .count() >= 4;
    }
}
