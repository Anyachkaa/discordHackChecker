package ru.itskekoff.hackchecker.framework.types;

import lombok.Setter;
import org.objectweb.asm.tree.*;
import ru.itskekoff.hackchecker.framework.utils.ClassUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Setter
public abstract class Check extends ClassUtils {
    public Map<String, byte[]> files;
    public Map<String, ClassNode> classes;

    public abstract ScanReport process();

    public void fill(Map<String, ClassNode> classes, Map<String, byte[]> files) {
        this.classes = classes;
        this.files = files;
    }

    public void clear() {
        this.classes = null;
        this.files = null;
    }

    public boolean isClassContains(String name) {
        return classes.keySet().stream().anyMatch(file -> file.equals(name));
    }

    public boolean isFileContains(String name) {
        return files.keySet().stream().anyMatch(file -> file.equals(name));
    }
}
