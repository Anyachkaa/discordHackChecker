package ru.itskekoff.hackchecker.framework.types;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ScanReport {
    private String message;
    private Priority priority;
}
