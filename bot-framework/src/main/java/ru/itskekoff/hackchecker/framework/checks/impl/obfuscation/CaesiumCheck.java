package ru.itskekoff.hackchecker.framework.checks.impl.obfuscation;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class CaesiumCheck extends Check {
    private static final String BSM_DESC_START = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;";
    private static final String BSM_DESC_END = ")Ljava/lang/Object;";

    @Override
    public ScanReport process() {
        AtomicBoolean foundObfuscation = new AtomicBoolean(false);
        classes.values().forEach(classNode -> {
            if (getStringsMethod(classNode) != null) {
                foundObfuscation.set(true);
            }
            classNode.methods.forEach(methodNode -> Arrays.stream(methodNode.instructions.toArray())
                    .filter(node -> node instanceof InvokeDynamicInsnNode)
                    .map(InvokeDynamicInsnNode.class::cast)
                    .filter(node -> node.bsm.getDesc().startsWith(BSM_DESC_START))
                    .filter(node -> node.bsm.getDesc().endsWith(BSM_DESC_END))
                    .forEach(node -> {
                        ClassNode handle = classes.get(node.bsm.getOwner());
                        if (handle == null)
                            return;
                        handle.methods.stream()
                                .filter(method -> method.name.equals(node.bsm.getName()))
                                .filter(method -> method.desc.equals(node.bsm.getDesc()))
                                .findAny().ifPresent(bootstrap -> foundObfuscation.set(true));
                    }));
        });

        if (foundObfuscation.get()) {
            return new ScanReport("Плагин содержит обфускацию Caesium", Priority.MODERATE);
        }
        return null;
    }

    private MethodNode getStringsMethod(ClassNode classNode) {
        return classNode.methods.stream()
                .filter(methodNode -> methodNode.desc.equals("()V"))
                .filter(methodNode -> Arrays.stream(methodNode.instructions.toArray()).anyMatch(node -> isString(node) && getString(node).equals("DES/CBC/PKCS5Padding")))
                .filter(methodNode -> isAccess(methodNode.access, ACC_STATIC))
                .findFirst()
                .orElse(null);
    }
}
