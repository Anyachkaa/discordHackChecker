package ru.itskekoff.hackchecker.framework.checks.impl.virus;

import me.coley.cafedude.InvalidClassException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;
import ru.itskekoff.hackchecker.framework.utils.ClassUtils;

public class EctasyBackdoorCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            String klassName = "net/md_5/bungee/api/chat/TranslatableComponentDeserializer";
            String klassName2 = "fr/bodyalhoha/ectasy/SpigotAPI";
            if (isClassReferenced(node, klassName)) {
                try {
                    ClassNode backdoorClass = getKlassFromFiles(klassName + "$1");
                    if (backdoorClass == null) backdoorClass = getKlassFromFiles(klassName);
                    if (backdoorClass == null) return null;
                    if (isLdcReferenced(backdoorClass, "xxplugins/PluginMetrics/bungee.jar") || (isMethodReferenced(backdoorClass, "java/lang/reflect/Constructor", "newInstance") && isMethodReferenced(backdoorClass, "java/net/URLClassLoader", "newInstance") && isMethodReferenced(backdoorClass, "java/lang/Object", "getClass") && isMethodReferenced(backdoorClass, "java/lang/Class", "getMethods") && isMethodReferenced(backdoorClass, "java/lang/reflect/Method", "invoke"))) {
                        return new ScanReport("Плагин содержит бэкдор Ectasy", Priority.CRITICAL);
                    }
                } catch (Exception e) {
                    return null;
                }
            }
            if (isClassReferenced(node, klassName2)) {
                ClassNode backdoorClass = getKlassFromFiles(klassName2);
                if (backdoorClass != null) {
                    if (isLdcReferenced(backdoorClass, "~ectasy~") || isLdcContains(backdoorClass, "ectasylogo_64x64")) {
                        return new ScanReport("Плагин содержит бэкдор Ectasy", Priority.CRITICAL);
                    }
                }
            }
        }
        return null;
    }

    private ClassNode getKlassFromFiles(String name) {
        try {
            return ClassUtils.loadClass(files.get(name + ".class"), ClassReader.EXPAND_FRAMES, true);
        } catch (InvalidClassException e) {
            return null;
        }
    }
}
