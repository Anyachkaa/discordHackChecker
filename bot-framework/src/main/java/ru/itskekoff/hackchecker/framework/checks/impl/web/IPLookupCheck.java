package ru.itskekoff.hackchecker.framework.checks.impl.web;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

public class IPLookupCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if (isLdcContains(node, "checkip.amazonaws.com", "ip-api.com",
                    "api.ipify.org", "extreme-ip-lookup.com") ||
                isMethodReferenced(node, "java/net/Socket", "getLocalAddress")) {
                return new ScanReport("Плагин получает информацию о айпи машины на которой работает сервер", Priority.HIGH);
            }
        }
        return null;
    }
}
