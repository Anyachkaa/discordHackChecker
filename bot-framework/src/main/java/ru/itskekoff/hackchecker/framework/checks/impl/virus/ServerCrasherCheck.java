package ru.itskekoff.hackchecker.framework.checks.impl.virus;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

public class ServerCrasherCheck extends Check {
    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            for (MethodNode method : node.methods) {
                if (detectCrasher(method)) {
                    return new ScanReport("Плагин может крашнуть/залагать сервер", Priority.HIGH);
                }
            }
        }
        return null;
    }

    public boolean detectCrasher(MethodNode node) {
        try {
            for (AbstractInsnNode abstractInsnNode : node.instructions.toArray()) {
                if (!(abstractInsnNode instanceof MethodInsnNode methodInsnNode)) {
                    continue;
                }
                if (methodInsnNode.owner.equals("org/bukkit/entityPlayer")) {
                    if (methodInsnNode.name.equals("setHealthScale") && methodInsnNode.desc.equals("(D)V")) {
                        AbstractInsnNode previous = methodInsnNode.getPrevious();
                        if (previous.getOpcode() == Opcodes.LDC) {
                            return true;
                        }
                    }
                }
                if (methodInsnNode.owner.equals("java/lang/Thread")
                        || methodInsnNode.owner.equals("java/util/concurrent/TimeUnit")) {
                    if (methodInsnNode.name.equals("sleep") && methodInsnNode.desc.equals("(J)V")) {
                        AbstractInsnNode previous = methodInsnNode.getPrevious();
                        if (previous.getOpcode() == Opcodes.LDC) {
                            LdcInsnNode ldcNode = (LdcInsnNode) previous;
                            if (ldcNode.cst instanceof Long l) {
                                if (l == 9223372036854775807L) {
                                    return true;
                                }
                            }
                        }
                    }
                }
                if (methodInsnNode.owner.equals("org/bukkit/Bukkit")) {
                    if (methodInsnNode.name.equals("createInventory") && methodInsnNode.desc.equals("(Lorg/bukkit/inventory/InventoryHolder;I)Lorg/bukkit/inventory/Inventory;")) {
                        AbstractInsnNode previous = methodInsnNode.getPrevious();
                        if (previous.getOpcode() == Opcodes.LDC) {
                            LdcInsnNode ldcNode = (LdcInsnNode) previous;
                            int i = (int) ldcNode.cst;
                            if (i == 1152000) {
                                return true;
                            }
                        }
                    }
                }
                if (methodInsnNode.owner.startsWith("net/minecraft/server/")
                        && methodInsnNode.owner.endsWith("PacketPlayOutExplosion")) {
                    if (!(methodInsnNode.getPrevious() instanceof MethodInsnNode method)) {
                        continue;
                    }
                    if (method.owner.startsWith("net/minecraft/server") && method.owner.endsWith("Vec3D")) {
                        if (!(method.getPrevious() instanceof LdcInsnNode)) {
                            continue;
                        }
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
