package ru.itskekoff.hackchecker.bot.utils;

import java.awt.*;

public class ColorUtils {
    public static Color getColor(String hexValue) {
        if (hexValue.equals("null") || hexValue.equals("none") || hexValue.isEmpty()) {
            return null;
        }
        return Color.decode(hexValue);
    }
}
