package ru.itskekoff.hackchecker.framework.checks.impl.virus;

import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

public class DiscordStealerCheck extends Check {

    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            if ((isLdcReferenced(node, "https://discordapp.com/api/v9/users/@me") &&
                    isLdcReferenced(node, "https://decryptionserver.ixixlliilxilili.repl.co")) ||
                    (isLdcReferenced(node, "360Browser")
                            && isLdcReferenced(node, "User Data") &&
                            isLdcReferenced(node, "Default") &&
                            isLdcReferenced(node, "Local State")) ||
                    isLdcContains(node, "dQw4w9WgXcQ") && isLdcContains(node, "leveldb") &&
                            (isLdcContains(node, "discord") || isLdcContains(node, ".ldb"))) {
                return new ScanReport("Обнаружен стиллер Discord токена", Priority.CRITICAL);
            }
        }
        return null;
    }
}
