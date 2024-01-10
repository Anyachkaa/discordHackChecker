package ru.itskekoff.hackchecker.framework.utils;

import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

public class ReflectionUtils {
    public static <T> List<? extends T> getClasses(String prefix, Class<T> classType) {
        return new Reflections(prefix).getSubTypesOf(classType).stream().map(c -> {
            try {
                return c.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }

            return null;
        }).collect(Collectors.toList());
    }
}
