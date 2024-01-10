package ru.itskekoff.hackchecker.framework.checks.impl.virus;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;
import ru.itskekoff.hackchecker.framework.utils.ClassUtils;

public class UpdaterCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (isDescriptorContains(node, "LUpdater;") ||
                    (isMethodReferenced(node, "Updater", "init") &&
                            isClassContains("Updater")) ||
                    isFileContains("plugin-config.bin")) {
                return new ScanReport("Плагин заражен вирусом Updater", Priority.CRITICAL);
            }
        }
        return null;
    }
}
