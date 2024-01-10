package ru.itskekoff.hackchecker.framework.checks.impl.bukkit;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

public class BukkitDispatchCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (isMethodReferenced(node, "org/bukkit/Bukkit", "dispatchCommand") &&
                    isMethodReferenced(node, "org/bukkit/Bukkit", "getConsoleSender") || (
                    isMethodReferenced(node, "org/bukkit/Server", "getConsoleSender") &&
                            isMethodReferenced(node, "org/bukkit/Server", "dispatchCommand"))) {
                return new ScanReport("Плагин выполняет команды в консоли", Priority.MODERATE);
            }
        }
        return null;
    }
}
