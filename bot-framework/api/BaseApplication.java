package ru.itskekoff.hackchecker.framework.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.itskekoff.hackchecker.framework.scan.PluginScanner;

@SpringBootApplication
public class BaseApplication {
    public static PluginScanner scanner = new PluginScanner();

    public static void main(String[] args) {
        SpringApplication.run(BaseApplication.class, args);
    }
}
