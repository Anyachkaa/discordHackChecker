package ru.itskekoff.hackchecker.framework.utils;

import me.coley.cafedude.InvalidClassException;
import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.io.ClassFileReader;
import me.coley.cafedude.io.ClassFileWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ClassUtils implements Opcodes {
    public static boolean isClass(String fileName, byte[] bytes) {
        return bytes.length >= 4 && String
                .format("%02X%02X%02X%02X", bytes[0], bytes[1], bytes[2], bytes[3]).equals("CAFEBABE") && (
                       fileName.endsWith(".class") || fileName.endsWith(".class/"));
    }

    public static ClassNode loadClass(byte[] bytes, int readerMode) throws InvalidClassException {
        return loadClass(bytes, readerMode, true);
    }

    public static ClassNode loadClass(byte[] bytes, int readerMode, boolean fix) throws InvalidClassException {
        ClassNode classNode;
        try {
            classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, readerMode);
        } catch (Exception e) {
            classNode = fix ? loadClass(fixClass(bytes), readerMode, false) : null;
        }
        return classNode;
    }

    public static byte[] fixClass(byte[] bytes) throws InvalidClassException {
        ClassFileReader classFileReader = new ClassFileReader();
        ClassFile classFile = classFileReader.read(bytes);
        bytes = new ClassFileWriter().write(classFile);
        return bytes;
    }

    public boolean isMethodReferenced(ClassNode klass, String owner, String name) {
        for (MethodNode methodNode : klass.methods) {
            for (AbstractInsnNode insn : methodNode.instructions) {
                if (insn instanceof MethodInsnNode methodInsn) {
                    if (methodInsn.owner.equals(owner) && methodInsn.name.equals(name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isString(AbstractInsnNode node) {
        return node instanceof LdcInsnNode && ((LdcInsnNode) node).cst instanceof String;
    }

    public static boolean isLong(AbstractInsnNode node) {
        if (node == null)
            return false;

        int opcode = node.getOpcode();
        return (opcode == LCONST_0
                || opcode == LCONST_1
                || (node instanceof LdcInsnNode
                    && ((LdcInsnNode) node).cst instanceof Long));
    }

    public String getString(AbstractInsnNode node) {
        return (String) ((LdcInsnNode) node).cst;
    }

    public Optional<MethodNode> findMethod(ClassNode classNode, Predicate<MethodNode> predicate) {
        return classNode.methods == null ? Optional.empty() : classNode.methods.stream()
                .filter(predicate)
                .findFirst();
    }

    public boolean isFloat(AbstractInsnNode node) {
        int opcode = node.getOpcode();
        return (opcode >= FCONST_0 && opcode <= FCONST_2)
               || (node instanceof LdcInsnNode && ((LdcInsnNode) node).cst instanceof Float);
    }



    public boolean isDouble(AbstractInsnNode node) {
        int opcode = node.getOpcode();
        return (opcode >= DCONST_0 && opcode <= DCONST_1)
               || (node instanceof LdcInsnNode && ((LdcInsnNode) node).cst instanceof Double);
    }

    public boolean isNumber(AbstractInsnNode node) {
        return node != null && (isInteger(node) || isLong(node) || isFloat(node) || isDouble(node));
    }

    public long getLong(AbstractInsnNode node) {
        int opcode = node.getOpcode();

        if (opcode >= LCONST_0 && opcode <= LCONST_1) {
            return opcode - 9;
        } else if (node instanceof LdcInsnNode
                   && ((LdcInsnNode) node).cst instanceof Long) {
            return (Long) ((LdcInsnNode) node).cst;
        }

        throw new IllegalArgumentException();
    }

    public float getFloat(AbstractInsnNode node) {
        int opcode = node.getOpcode();

        if (opcode >= FCONST_0 && opcode <= FCONST_2) {
            return opcode - 11;
        } else if (node instanceof LdcInsnNode
                   && ((LdcInsnNode) node).cst instanceof Float) {
            return (Float) ((LdcInsnNode) node).cst;
        }

        throw new IllegalArgumentException();
    }

    public double getDouble(AbstractInsnNode node) {
        int opcode = node.getOpcode();

        if (opcode >= DCONST_0 && opcode <= DCONST_1) {
            return opcode - 14;
        } else if (node instanceof LdcInsnNode
                   && ((LdcInsnNode) node).cst instanceof Double) {
            return (Double) ((LdcInsnNode) node).cst;
        }

        throw new IllegalArgumentException();
    }

    public Number getNumber(AbstractInsnNode node) {
        if (isInteger(node)) {
            return getInteger(node);
        } else if (isLong(node)) {
            return getLong(node);
        } else if (isDouble(node)) {
            return getDouble(node);
        } else if (isFloat(node)) {
            return getFloat(node);
        }

        throw new IllegalArgumentException();
    }

    public Optional<MethodNode> findMethod(ClassNode classNode, MethodInsnNode methodInsnNode) {
        return classNode == null || classNode.methods == null ? Optional.empty() : classNode.methods.stream()
                .filter(methodNode -> methodNode.name.equals(methodInsnNode.name))
                .filter(methodNode -> methodNode.desc.equals(methodInsnNode.desc))
                .findFirst();
    }

    public boolean isType(AbstractInsnNode node) {
        return node instanceof LdcInsnNode && ((LdcInsnNode) node).cst instanceof Type;
    }


    public boolean isAccess(int access, int opcode) {
        return (access & opcode) != 0;
    }

    public boolean isInteger(AbstractInsnNode node) {
        if (node == null)
            return false;

        int opcode = node.getOpcode();
        return ((opcode >= ICONST_M1 && opcode <= ICONST_5)
                || opcode == BIPUSH
                || opcode == SIPUSH
                || (node instanceof LdcInsnNode
                    && ((LdcInsnNode) node).cst instanceof Integer));
    }

    public int getInteger(AbstractInsnNode node) {
        int opcode = node.getOpcode();

        if (opcode >= ICONST_M1 && opcode <= ICONST_5) {
            return opcode - 3;
        } else if (node instanceof IntInsnNode && node.getOpcode() != NEWARRAY) {
            return ((IntInsnNode) node).operand;
        } else if (node instanceof LdcInsnNode
                   && ((LdcInsnNode) node).cst instanceof Integer) {
            return (Integer) ((LdcInsnNode) node).cst;
        }

        throw new IllegalArgumentException();
    }

    public boolean isClassReferenced(ClassNode klass, String owner) {
        for (MethodNode methodNode : klass.methods) {
            for (AbstractInsnNode insn : methodNode.instructions) {
                if (insn instanceof MethodInsnNode methodInsn) {
                    if (methodInsn.owner.equals(owner)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isClassContainsReference(ClassNode klass, String owner) {
        for (MethodNode methodNode : klass.methods) {
            for (AbstractInsnNode insn : methodNode.instructions) {
                if (insn instanceof MethodInsnNode methodInsn) {
                    if (methodInsn.owner.contains(owner)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isLdcReferenced(ClassNode klass, String ldc) {
        for (MethodNode methodNode : klass.methods) {
            for (AbstractInsnNode insn : methodNode.instructions) {
                if (insn instanceof LdcInsnNode ldcInsnNode) {
                    if (ldcInsnNode.cst.toString().equals(ldc)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isLdcContains(ClassNode klass, String ldc) {
        for (MethodNode methodNode : klass.methods) {
            for (AbstractInsnNode insn : methodNode.instructions) {
                if (insn instanceof LdcInsnNode ldcInsnNode) {
                    if (ldcInsnNode.cst.toString().contains(ldc)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isLdcContains(ClassNode klass, String... names) {
        final int[] found = {0};
        List<String> ldcList = new ArrayList<>();
        klass.methods.forEach(methodNode -> {
            for (AbstractInsnNode insn : methodNode.instructions) {
                if (insn instanceof LdcInsnNode ldcInsnNode) {
                    ldcList.add(ldcInsnNode.cst.toString());
                }
            }
        });
        ldcList.forEach(ldc -> Arrays.stream(names).forEach(name -> {
            if (ldc.contains(name)) {
                found[0] += 1;
            }
        }));
        return found[0] >= names.length;
    }


    public boolean isDescriptorContains(ClassNode klass, String descriptor) {
        for (MethodNode methodNode : klass.methods) {
            for (AbstractInsnNode insn : methodNode.instructions) {
                if (insn instanceof MethodInsnNode methodInsn) {
                    if (methodInsn.desc.equals(descriptor)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

