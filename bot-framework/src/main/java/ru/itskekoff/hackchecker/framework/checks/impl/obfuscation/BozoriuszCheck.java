package ru.itskekoff.hackchecker.framework.checks.impl.obfuscation;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

import java.util.Arrays;
import java.util.function.Predicate;

public class BozoriuszCheck extends Check {
    private final Predicate<String> namePredicate = (s -> s.split("\u0001/", 69).length > 3 || s.split("\u0020").length > 3);
    private final Predicate<ClassNode> contentPredicate = (cn -> (!cn.methods.isEmpty() && cn.methods.stream().anyMatch(methodNode -> methodNode.desc.equals("(\u0001/)L\u0001/;"))));

    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (files.entrySet().stream().anyMatch(entry -> namePredicate.test(entry.getKey()))
                || contentPredicate.test(node) || node.fields.stream().anyMatch(field -> (field.name.equals("Ꮸ") || field.name.matches("[Il]{50,}")
                                                                                          || ((int) field.name.charAt(0) >= '\u3050' && (int) field.name.charAt(0) <= '\u5094'))
                                                                                         && field.desc.equals("J"))) {
                return new ScanReport("Плагин содержит обфускацию Bozar", Priority.MODERATE);
            }

        }
        boolean foundObfuscation;
        for (ClassNode node : classes.values()) {
            foundObfuscation = node.methods.stream()
                    .anyMatch(methodNode -> Arrays.stream(methodNode.instructions.toArray())
                            .filter(insn -> insn.getOpcode() == NEW)
                            .map(TypeInsnNode.class::cast)
                            .filter(insn -> insn.desc.equals("java/lang/String"))
                            .filter(insn -> insn.getNext().getOpcode() == DUP)
                            .filter(insn -> insn.getNext().getNext().getOpcode() == ALOAD)
                            .filter(insn -> insn.getNext().getNext().getNext().getOpcode() == INVOKESPECIAL)
                            .anyMatch(insn -> {
                                try {
                                    int startIndex;
                                    startIndex = methodNode.instructions.indexOf(insn);
                                    return startIndex != -1 && isInteger(insn.getPrevious().getPrevious());
                                } catch (Exception e) {
                                    return false;
                                }
                            }));

            if (foundObfuscation) {
                return new ScanReport("Плагин содержит обфускацию Bozar", Priority.MODERATE);
            }
        }
        return null;
    }
}
