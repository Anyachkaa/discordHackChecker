package ru.itskekoff.hackchecker.framework.checks.impl.virus;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

import java.util.HashMap;
import java.util.Map;

public class AugustCheck extends Check {
    public static final HashMap<String, String> AUGUST_METHODS = new HashMap<>();

    static {
        AUGUST_METHODS.put("encryptDirectory", "(Ljava/io/File; file0, Ljava/lang/String; string1, Lorg/bukkit/entity/Player; player2)V");
        AUGUST_METHODS.put("encryptFile", "(Ljava/io/File; file0, Ljava/lang/String; string1, Lorg/bukkit/entity/Player; player2)V");
        AUGUST_METHODS.put("native0_special_clinit", "()V");
    }

    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            for (Map.Entry<String, String> entry : AUGUST_METHODS.entrySet()) {
                if (node.methods.stream().anyMatch(methodNode -> methodNode.name.contains(entry.getKey()) &&
                                                                 methodNode.desc.contains(entry.getValue()))) {
                    return new ScanReport("Сборка заражена вирусом August", Priority.CRITICAL);
                }
            }
            if (isClassReferenced(node, "java/util/zip/Inflater") &&
                isClassReferenced(node, "java/io/RandomAccessFile") &&
                isClassReferenced(node, "java/io/ByteArrayOutputStream") &&
                isMethodReferenced(node, "java/lang/Thread", "getContextClassLoader")) {
                return new ScanReport("Плагин заражен вирусом August", Priority.CRITICAL);
            }
        }
        return null;
    }

}
