package ru.itskekoff.hackchecker.framework.checks.impl.bukkit;

import org.objectweb.asm.tree.*;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;


public class BukkitSetOpCheck extends Check {
    private static final List<String> METHOD_INSN_NODE_OWNERS = new ArrayList<>();

    static {
        METHOD_INSN_NODE_OWNERS.add("org/bukkit/entity/Player");
        METHOD_INSN_NODE_OWNERS.add("org/bukkit/OfflinePlayer");
        METHOD_INSN_NODE_OWNERS.add("org/bukkit/command/CommandSender");
        METHOD_INSN_NODE_OWNERS.add("org/bukkit/permissions/ServerOperator");
    }


    @Override
    public ScanReport process() {
        for (ClassNode node : classes.values()) {
            for (MethodNode method : node.methods) {
                if (detectForceOp(method)) {
                    return new ScanReport("Плагин может выдать опку", Priority.HIGH);
                }
            }
        }
        return null;
    }

    private boolean detectForceOp(MethodNode methodNode) {

        for (AbstractInsnNode instruction : methodNode.instructions) {
            if (instruction instanceof MethodInsnNode methodInsnNode) {
                if (methodInsnNode.owner.equals("org/bukkit/entity/Player")
                        || methodInsnNode.owner.equals("org/bukkit/OfflinePlayer")
                        || methodInsnNode.owner.equals("org/bukkit/command/CommandSender")
                        || methodInsnNode.owner.equals("org/bukkit/permissions/ServerOperator")) {
                    if (methodInsnNode.name.equals("setOp") && methodInsnNode.desc.equals("(Z)V")) {
                        AbstractInsnNode previous = methodInsnNode.getPrevious();
                        if (previous.getOpcode() == IALOAD) {
                            if (matches(previous.getPrevious(), 1)) {
                                return true;
                            }
                        }
                    }
                }
                if (METHOD_INSN_NODE_OWNERS.contains(methodInsnNode.owner)) {
                    if (!(methodInsnNode.name.equals("setOp") && methodInsnNode.desc.equals("(Z)V"))) {
                        continue;
                    }
                    AbstractInsnNode previous = methodInsnNode.getPrevious();
                    if (matches(previous, 1)) {
                        return true;
                    }
                }
            }
        }
        for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
            if (instruction instanceof LdcInsnNode ldcInsnNode) {
                if (!(ldcInsnNode.cst instanceof String value)) {
                    continue;
                }
                if (!value.equalsIgnoreCase("setOp")) {
                    continue;
                }
                AbstractInsnNode next = instruction.getNext();
                if (!(next.getOpcode() == ICONST_1)) {
                    continue;
                }
                AbstractInsnNode nextNext = next.getNext();
                if (!(nextNext.getOpcode() == ANEWARRAY)) {
                    continue;
                }
                TypeInsnNode typeInsnNode = (TypeInsnNode) nextNext;
                if (!typeInsnNode.desc.equals(("java/lang/Class"))) {
                    continue;
                }
                AbstractInsnNode nextNextNext = nextNext.getNext();
                if (nextNextNext.getOpcode() != DUP) {
                    continue;
                }
                AbstractInsnNode nextNextNextNext = nextNextNext.getNext();
                if (nextNextNextNext.getOpcode() != ICONST_0) {
                    continue;
                }
                AbstractInsnNode nextNextNextNextNext = nextNextNextNext.getNext();
                if (nextNextNextNextNext.getOpcode() != GETSTATIC) {
                    continue;
                }
                FieldInsnNode fieldInsnNode = (FieldInsnNode) nextNextNextNextNext;
                if (!fieldInsnNode.owner.equals("java/lang/Boolean") && !fieldInsnNode.name.equals("TYPE") && !fieldInsnNode.desc.equals("Ljava/lang/Class")) {
                    continue;
                }
                AbstractInsnNode nextNextNextNextNextNext = nextNextNextNextNext.getNext();
                if (nextNextNextNextNextNext.getOpcode() != AASTORE) {
                    continue;
                }
                AbstractInsnNode nextNextNextNextNextNextNext = nextNextNextNextNextNext.getNext();
                if (nextNextNextNextNextNextNext.getOpcode() != INVOKEVIRTUAL) {
                    continue;
                }
                MethodInsnNode methodInsnNode = (MethodInsnNode) nextNextNextNextNextNextNext;
                if (!methodInsnNode.owner.equals("java/lang/Class") && !methodInsnNode.name.equals("getMethod")
                        && !methodInsnNode.desc.equals("(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;")) {
                    continue;
                }
                return true;
            }
        }
        for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
            if (!(instruction instanceof LdcInsnNode ldcInsnNode)) {
                continue;
            }
            if (!(ldcInsnNode.cst instanceof String string)) {
                continue;
            }
            if (string.contains("ops.json")) {
                return true;
            }
        }
        for (AbstractInsnNode instruction : methodNode.instructions) {
            if (!(instruction instanceof MethodInsnNode methodInsnNode)) {
                continue;
            }
            if (isMethodInsnNodeCorrect(methodInsnNode, "org/bukkit/entity/Player", "isOp", "()Z")) {
                try {
                    if (!(methodInsnNode.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext() instanceof MethodInsnNode node)) {
                        continue;
                    }
                    if (isMethodInsnNodeCorrect(node, "org/bukkit/entity/Player", "setOp", "(Z)V")) {
                        return true;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return false;
    }

    public static boolean matches(AbstractInsnNode node, int value) {
        int opCode = node.getOpcode();
        if (opCode == ICONST_0 && value == 0
                || opCode == ICONST_1 && value == 1
                || opCode == ICONST_2 && value == 2
                || opCode == ICONST_3 && value == 3
                || opCode == ICONST_4 && value == 4
                || opCode == ICONST_5 && value == 5) {
            return true;
        }
        if (opCode == POP2) {
            return true;
        }
        if (opCode == BIPUSH || opCode == SIPUSH) {
            return ((IntInsnNode) node).operand == value;
        } else if (node.getOpcode() == LDC) {
            Object cst = ((LdcInsnNode) node).cst;
            return cst instanceof Integer && cst.equals(value);
        }
        return false;
    }

    public static boolean isMethodInsnNodeCorrect(MethodInsnNode methodInsnNode, String owner, String name, String desc) {
        return methodInsnNode.owner.equals(owner) && methodInsnNode.name.equals(name) && methodInsnNode.desc.equals(desc);
    }
}
