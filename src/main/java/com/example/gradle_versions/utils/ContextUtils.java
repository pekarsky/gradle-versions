package com.example.gradle_versions.utils;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ContextUtils {

    private static final String FILE_CONTEXT_TEMPLATE = """
            <#file_name>
            File: #file_name
            Path: #file_path
            Size: #file_size bytes
            Created: #file_creation_date
            Modified: #file_modification_date
            
            Content:
            #file_content
            </#file_name>
            
            """;

    public static String processFile(Path file, String relativePath) throws IOException {
        if(Files.notExists(file)) {
            return "";
        }

        String content = new String(Files.readAllBytes(file));
        String fileName = file.getFileName().toString();
        String filePath = file.toString();
        long fileSize = Files.size(file);

        BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
        String creationDate = formatDate(attrs.creationTime().toInstant());
        String modificationDate = formatDate(attrs.lastModifiedTime().toInstant());

        return FILE_CONTEXT_TEMPLATE
                .replace("#file_name", fileName)
                .replace("#file_path", filePath)
                .replace("#file_relative_path", relativePath)
                .replace("#file_size", String.valueOf(fileSize))
                .replace("#file_creation_date", creationDate)
                .replace("#file_modification_date", modificationDate)
                .replace("#file_content", content);
    }

    private static String formatDate(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}

