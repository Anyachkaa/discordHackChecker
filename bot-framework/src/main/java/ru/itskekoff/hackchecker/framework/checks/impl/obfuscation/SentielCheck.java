package ru.itskekoff.hackchecker.framework.checks.impl.obfuscation;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

import java.util.Arrays;

public class SentielCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode classNode : classes.values()) {
            MethodNode decryptMethod = classNode.methods.stream()
                    .filter(methodNode -> methodNode.desc.equals("(Ljava/lang/String;)Ljava/lang/String;"))
                    .filter(methodNode -> methodNode.access == ACC_PUBLIC + ACC_STATIC)
                    .filter(methodNode -> methodNode.maxLocals == 4)
                    .filter(methodNode -> methodNode.maxStack == 4)
                    .findFirst().orElse(null);
            if (decryptMethod != null) {
                if (classNode.methods.stream()
                        .flatMap(methodNode -> Arrays.stream(methodNode.instructions.toArray()))
                        .filter(node -> node instanceof MethodInsnNode)
                        .map(MethodInsnNode.class::cast)
                        .anyMatch(node -> node.name.equals(decryptMethod.name) &&
                                          node.desc.equals(decryptMethod.desc) &&
                                          isString(node.getPrevious()))) {
                    return new ScanReport("Плагин содержит обфускацию Sentiel", Priority.MODERATE);
                }
            }
        }
        return null;
    }
}
