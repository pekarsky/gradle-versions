package com.example.gradle_versions.ai_tools;

import com.example.gradle_versions.service.GithubFileDownloader;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
@Log4j
public class GradleConfigTool {

    private final GithubFileDownloader githubFileDownloader;
    @Value("${projectsRootDir}")
    private String projectsRootDir;
    private final Collection<String> GRADLE_CONFIGS = List.of("build.gradle", "gradle.properties");

//    @Tool("Downloads build.gradle file from GitHub for the specified project")
    public String getGradleConfig(String projectName) {
        try {
            return githubFileDownloader.getFileContent(projectName, "development", "build.gradle");
        } catch (Exception e) {
            log.error("Error downloading file", e);
        }
        return null;
    }

    @Tool("Downloads build.gradle file from GitHub for the specified project from local filesystem")
    public String getGradleConfigFromLocal(String projectName) {
        String projectPath = projectsRootDir + "/" + projectName;

        StringBuilder sb = new StringBuilder();

        for (String configFile: GRADLE_CONFIGS) {
            Path path = Path.of(projectPath, configFile);
            if (Files.exists(path)) {
                try {
                   String content = new String(Files.readAllBytes(path));
                   sb.append("<file name=\"").append(configFile).append("\">\n");
                   sb.append(content).append("</file>\n");
                } catch (IOException e) {
                    log.error("Error reading file: " + path, e);
                }
            }
        }
        return sb.toString();
    }
}
