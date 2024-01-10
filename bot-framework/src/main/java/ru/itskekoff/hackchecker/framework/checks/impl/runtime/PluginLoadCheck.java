package ru.itskekoff.hackchecker.framework.checks.impl.runtime;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

public class PluginLoadCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (isMethodReferenced(node, "org/bukkit/plugin/PluginManager", "loadPlugin") ||
                    isMethodReferenced(node, "org/bukkit/plugin/PluginManager", "loadPlugins")) {
                return new ScanReport("Плагин может загрузить другие плагины во время работы", Priority.HIGH);
            }
        }
        return null;
    }
}
