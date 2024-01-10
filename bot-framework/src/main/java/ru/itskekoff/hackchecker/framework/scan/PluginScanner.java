package ru.itskekoff.hackchecker.framework.scan;

import lombok.Getter;
import lombok.Setter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import ru.itskekoff.hackchecker.framework.checks.CheckManager;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.types.exception.InvalidPluginException;
import ru.itskekoff.hackchecker.framework.types.ScanReport;
import ru.itskekoff.hackchecker.framework.types.ScanResult;
import ru.itskekoff.hackchecker.framework.utils.ClassUtils;
import ru.itskekoff.hackchecker.framework.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class PluginScanner {
    private final Map<String, ClassNode> classes = new HashMap<>();
    private final Map<String, byte[]> files = new HashMap<>();
    private final int readerFlags = ClassReader.EXPAND_FRAMES;
    private final CheckManager manager;

    public PluginScanner() {
        this.manager = new CheckManager();
        this.manager.registerChecks();
    }

    public ScanResult analyze(File pluginFile) throws Exception {
        loadPlugin(pluginFile);
        if (classes.isEmpty()) {
            throw new InvalidPluginException();
        }
        if (files.containsKey("plugin.yml") || files.containsKey("bungee.yml")) {
            byte[] data;
            data = files.get(files.containsKey("bungee.yml") ? "bungee.yml" : "plugin.yml");
            String mainClass = Arrays.stream(new String(data).split("\n"))
                    .filter(line -> line.startsWith("main: "))
                    .findFirst()
                    .orElse("none: 1")
                    .trim()
                    .split(": ")[1].split(" ")[0]
                    .replaceAll("\\.", "/")
                    .replaceAll("\"", "")
                    .replaceAll("'", "");
            if (mainClass.equals("none: 1")) {
                throw new InvalidPluginException();
            }
            String[] parts = mainClass.split("/");
            String pluginPackage = mainClass.contains("/") ? parts[0] + (parts.length > 2 ? "/" + parts[1] : "/") : mainClass;
            files.entrySet().removeIf(file -> classes.entrySet().stream().anyMatch(klassName ->
                    klassName.getKey().contains(file.getKey())));
            classes.entrySet().removeIf(entry -> !entry.getKey().startsWith(pluginPackage));
            List<ScanReport> finalReports = new ArrayList<>();
            List<Check> checkList = new CopyOnWriteArrayList<>(manager.getChecks());
            checkList.forEach(check -> {
                check.fill(classes, files);
                ScanReport report = check.process();
                if (report != null) {
                    finalReports.add(report);
                }
                check.clear();
            });
            finalReports.sort(Comparator.comparingInt(r -> r.getPriority().ordinal()));
            return new ScanResult(finalReports);
        } else {
            throw new InvalidPluginException();
        }
    }

    private void loadPlugin(File pluginFile) throws IOException {
        classes.clear();
        files.clear();
        FileUtils.loadFilesFromZip(pluginFile.getAbsolutePath()).forEach((name, data) -> {
            try {
                if (ClassUtils.isClass(name, data)) {
                    ClassNode classNode = ClassUtils.loadClass(data, readerFlags);
                    classes.put(classNode.name, classNode);
                }
                files.put(name, data);
            } catch (Exception e) {
                files.put(name, data);
            }
        });
    }
}

