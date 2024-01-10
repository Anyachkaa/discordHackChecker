package ru.itskekoff.hackchecker.bot.utils;

import net.dv8tion.jda.api.entities.Message;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipFile;

public final class FileUtils {
    public static void downloadFile(String urlStr, String file) throws IOException {
        URLConnection connection = new URL(urlStr).openConnection();
        connection.addRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 YaBrowser/22.11.5.715 Yowser/2.5 Safari/537.36");
        connection.setRequestProperty("Content-Type", "text/html; charset=utf-8");
        try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
             FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        }
    }

    public static String getUniqueFileName(Message.Attachment attachment) throws IOException {
        String fileName = attachment.getFileName();
        if (new File("tmp/" + fileName).exists()) {
            while (new File("tmp/" + fileName).exists()) {
                fileName = attachment.getFileName().split("\\.")[0] + "_" + new Random().nextInt(99999999) + ".jar";
            }
        }
        return fileName;
    }
}
