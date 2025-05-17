package com.example.gradle_versions.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileUtils {
    private final static String REPORTS_ROOT = "reports";

    public String loadProjectReport(String projectName) {
        Path path = getPath(projectName);
        if (Files.exists(path)) {
            try {
                return new String(Files.readAllBytes(path));
            } catch (IOException e) {
                log.error("Error reading file: {}", path, e);
            }
        }
        return null;
    }

    public void saveProjectReport(String projectName, String report) {
        Path path = getPath(projectName);
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, report.getBytes());
        } catch (IOException e) {
            log.error("Error writing file: {}", path, e);
        }
    }

    private Path getPath(String projectName) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String filePath = String.format("%s/%s/%s.txt", REPORTS_ROOT, date, projectName);
        return Paths.get(filePath);
    }

    public boolean reportPathExists() {
        return Files.exists(getPath("dummy").getParent());
    }
}
