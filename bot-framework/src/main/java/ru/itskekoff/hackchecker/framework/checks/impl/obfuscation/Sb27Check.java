package ru.itskekoff.hackchecker.framework.checks.impl.obfuscation;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Sb27Check extends Check {
    @Override
    public ScanReport process() {
        AtomicBoolean foundInvokeDynamic = new AtomicBoolean(false);
        AtomicBoolean anyInvokesInClass = new AtomicBoolean(false);
        AtomicBoolean foundString = new AtomicBoolean(false);
        for (ClassNode classNode : classes.values()) {
            foundInvokeDynamic.set(classNode.methods.stream()
                    .filter(methodNode -> isAccess(methodNode.access, ACC_PRIVATE))
                    .filter(methodNode -> isAccess(methodNode.access, ACC_STATIC))
                    .filter(methodNode -> methodNode.desc.equals("()V"))
                    .anyMatch(methodNode -> Arrays.stream(methodNode.instructions.toArray())
                            .filter(node -> node.getOpcode() == GETSTATIC)
                            .filter(node -> isInteger(node.getNext()))
                            .filter(node -> isType(node.getNext().getNext()) || isString(node.getNext().getNext()))
                            .filter(node -> node.getNext().getNext().getNext().getOpcode() == AASTORE)
                            .map(FieldInsnNode.class::cast)
                            .anyMatch(node -> true)));
            anyInvokesInClass.set(classNode.methods.stream()
                    .anyMatch(methodNode -> Arrays.stream(methodNode.instructions.toArray())
                            .anyMatch(node -> node instanceof InvokeDynamicInsnNode)));
            foundString.set(classNode.methods.stream().anyMatch(methodNode ->
                    Arrays.stream(methodNode.instructions.toArray())
                            .filter(node -> node.getOpcode() == INVOKESTATIC)
                            .filter(node -> isString(node.getPrevious())) //key
                            .filter(node -> isString(node.getPrevious().getPrevious())) //encrypted
                            .map(MethodInsnNode.class::cast)
                            .filter(node -> node.owner.equals(classNode.name))
                            .filter(node -> node.desc.equals("(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"))
                            .anyMatch(node -> {
                                Optional<MethodNode> methodOptional = findMethod(classNode, method -> method.name.equals(node.name) && method.desc.equals(node.desc));
                                return methodOptional.isPresent();
                            })));
        }
        if ((foundInvokeDynamic.get() && anyInvokesInClass.get()) || foundString.get()) {
            return new ScanReport("Плагин содержит обфускацию Superblaubeere", Priority.MODERATE);
        }

        return null;
    }
}
