package ru.itskekoff.hackchecker.framework.api.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import ru.itskekoff.hackchecker.framework.api.BaseApplication;
import ru.itskekoff.hackchecker.framework.checks.impl.Priority;
import ru.itskekoff.hackchecker.framework.types.ScanReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class CheckRequestController {
    @PostMapping("/analyze")
    public ResponseEntity<List<MultiScanResult>> analyze(@RequestParam("file_data") MultipartFile[] files) {
        List<MultiScanResult> results = new ArrayList<>();
        try {
            if (files.length > 10) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
            for (MultipartFile file : files) {
                File pluginFile = convertMultiPartToFile(file);
                try {
                    MultiScanResult result = new MultiScanResult(file.getOriginalFilename(), file.getSize(), BaseApplication.scanner.analyze(pluginFile).getReports());
                    sortReports(result.getReports());
                    if (pluginFile.delete()) {
                        if (result.getReports().isEmpty()) {
                            result.getReports().add(new ScanReport("Плагин чистый по версии хак чекера", Priority.LOW));
                        }
                        results.add(result);
                    } else {
                        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                } catch (Exception e) {
                    results.add(new MultiScanResult(file.getOriginalFilename(), file.getSize(),
                            Collections.singletonList(new ScanReport("Ошибка проверки: " + e.getLocalizedMessage(),
                                    Priority.CRITICAL))));
                }
            }
            results.sort((r1, r2) -> {
                Map<Priority, Long> m1 = r1.getReports().stream()
                        .collect(Collectors.groupingBy(ScanReport::getPriority, Collectors.counting()));
                Map<Priority, Long> m2 = r2.getReports().stream()
                        .collect(Collectors.groupingBy(ScanReport::getPriority, Collectors.counting()));
                for (Priority priority : Priority.values()) {
                    long count1 = m1.getOrDefault(priority, 0L);
                    long count2 = m2.getOrDefault(priority, 0L);
                    if (count1 != count2) {
                        return Long.compare(count2, count1);
                    }
                }
                return 0;
            });
            return new ResponseEntity<>(results, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    public static void sortReports(List<ScanReport> reports) {
        Comparator<ScanReport> comparator = Comparator
                .comparing(ScanReport::getPriority,
                        Enum::compareTo);
        reports.sort(comparator);
    }

    @AllArgsConstructor
    @Getter
    public static class MultiScanResult {
        private String fileName;
        private long fileSize;
        private List<ScanReport> reports;
    }
}
