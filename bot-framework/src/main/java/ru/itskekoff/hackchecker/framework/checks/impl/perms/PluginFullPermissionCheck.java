package ru.itskekoff.hackchecker.framework.checks.impl.perms;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.ScanReport;
import ru.itskekoff.hackchecker.framework.utils.ClassUtils;

public class PluginFullPermissionCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (isLdcContains(node, "add *") || isLdcContains(node, "set *")) {
                return new ScanReport("Плагин может выдать * игроку путем выполнения команды", Priority.HIGH);
            }
        }
        return null;
    }
}
