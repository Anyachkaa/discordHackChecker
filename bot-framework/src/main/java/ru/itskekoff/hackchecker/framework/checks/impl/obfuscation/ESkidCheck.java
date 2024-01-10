package ru.itskekoff.hackchecker.framework.checks.impl.obfuscation;

import org.objectweb.asm.tree.*;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ESkidCheck extends Check {
    @Override
    public ScanReport process() {
        AtomicBoolean foundObfuscation = new AtomicBoolean(false);
        classes.values().forEach(classNode -> classNode.methods.forEach(methodNode -> {
            Arrays.stream(methodNode.instructions.toArray())
                    .forEach(insn -> {
                        if (isString(insn) && insn.getNext() != null
                            && insn.getNext().getOpcode() == INVOKESTATIC) {
                            if (((MethodInsnNode) insn.getNext()).desc.equals("(Ljava/lang/String;)Ljava/lang/String;")) {
                                MethodNode decMethod = classNode.methods.stream().filter(node ->
                                        node.name.equals(((MethodInsnNode) insn.getNext()).name)).findFirst().orElse(null);
                                if (decMethod != null) {
                                    int key = getKey(decMethod);
                                    if (key != 999999999) {
                                        foundObfuscation.set(true);
                                    } else {
                                        key = getKeyTamper(decMethod);
                                        if (key != 999999999) {
                                            foundObfuscation.set(true);
                                        }
                                    }
                                }
                            }
                        }
                    });
        }));
        if (foundObfuscation.get()) {
            return new ScanReport("Плагин содержит обфускацию ESkid", Priority.MODERATE);
        }
        return null;
    }


    private int getKey(MethodNode methodNode) {
        int[] key = {999999999};
        Arrays.stream(methodNode.instructions.toArray())
                .forEach(insn -> {
                    if (insn.getOpcode() == CALOAD && insn.getNext() != null
                        && isNumber(insn.getNext())
                        && insn.getNext().getNext().getOpcode() == ILOAD
                        && insn.getNext().getNext().getNext().getOpcode() == IXOR) {
                        key[0] = getNumber(insn.getNext()).intValue();
                    }
                });
        return key[0] != 999999999 ? key[0] : 999999999;
    }

    private int getKeyTamper(MethodNode methodNode) {
        int[] key = {999999999};
        Arrays.stream(methodNode.instructions.toArray())
                .forEach(insn -> {
                    if (insn.getOpcode() == INVOKEVIRTUAL && insn.getNext() != null
                        && insn.getNext().getOpcode() == ISHL
                        && isNumber(insn.getNext().getNext())
                        && insn.getNext().getNext().getNext().getOpcode() == IXOR
                        && insn.getNext().getNext().getNext().getNext().getOpcode() == ISTORE
                        && insn.getNext().getNext().getNext().getNext().getNext().getOpcode() == ALOAD
                        && insn.getNext().getNext().getNext().getNext().getNext().getNext().getOpcode() == ARRAYLENGTH) {
                        if (((MethodInsnNode) insn).name.equals("hashCode")) {
                            key[0] = (int) getNumber(insn.getNext().getNext());
                        }
                    }
                });
        return key[0] != 999999999 ? key[0] : 999999999;
    }

}
