package ru.itskekoff.hackchecker.framework.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ScanResult {
    private List<ScanReport> reports;
}
