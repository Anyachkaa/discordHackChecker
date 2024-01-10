package ru.itskekoff.hackchecker.framework.utils;

import java.io.IOException;
import java.util.*;
import java.util.zip.ZipFile;

public final class FileUtils {
    public static Map<String, byte[]> loadFilesFromZip(String file) throws IOException {
        Map<String, byte[]> files = new HashMap<>();

        try (ZipFile zipFile = new ZipFile(file)) {
            zipFile.entries().asIterator().forEachRemaining(zipEntry -> {
                try {
                    files.put(zipEntry.getName(), zipFile.getInputStream(zipEntry).readAllBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        return files;
    }
}
