package ru.itskekoff.hackchecker.framework.checks;

import lombok.Getter;
import ru.itskekoff.hackchecker.framework.types.Check;
import ru.itskekoff.hackchecker.framework.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CheckManager {
    private final List<Check> checks = new ArrayList<>();

    public void registerChecks() {
        checks.addAll(ReflectionUtils.getClasses("ru.itskekoff.hackchecker.framework.checks.impl", Check.class));
    }
}
