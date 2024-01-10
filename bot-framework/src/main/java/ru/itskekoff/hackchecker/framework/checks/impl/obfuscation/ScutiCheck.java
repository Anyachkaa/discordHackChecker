package ru.itskekoff.hackchecker.framework.checks.impl.obfuscation;

import org.objectweb.asm.tree.MethodInsnNode;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScutiCheck extends Check {
    @Override
    public ScanReport process() {
        AtomicBoolean foundObfuscation = new AtomicBoolean(false);
        foundObfuscation.set(classes.values().stream().anyMatch(classNode -> classNode.methods.stream().anyMatch(methodNode -> Arrays.stream(methodNode.instructions.toArray())
                .filter(node -> node instanceof MethodInsnNode)
                .filter(node -> node.getOpcode() == INVOKESTATIC)
                .map(MethodInsnNode.class::cast)
                .filter(node -> node.desc.equals("(Ljava/lang/String;I)Ljava/lang/String;"))
                .filter(node -> isInteger(node.getPrevious()))
                .anyMatch(node -> isString(node.getPrevious().getPrevious())))));
        if (!foundObfuscation.get()) {
            foundObfuscation.set(classes.values().stream().anyMatch(classNode -> classNode.methods.stream()
                    .filter(methodNode -> methodNode.name.length() > 50)
                    .filter(methodNode -> methodNode.desc.equals("(Ljava/lang/String;)Ljava/lang/String;"))
                    .anyMatch(methodNode -> methodNode.access == ACC_PRIVATE + ACC_STATIC + ACC_BRIDGE + ACC_BRIDGE)));
        }
        if (foundObfuscation.get()) {
            return new ScanReport("Плагин содержит обфускацию Scuti", Priority.MODERATE);
        }
        return null;
    }
}
