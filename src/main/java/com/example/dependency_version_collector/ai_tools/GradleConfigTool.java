package com.example.dependency_version_collector.ai_tools;

import com.example.dependency_version_collector.service.GithubFileDownloader;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j
public class GradleConfigTool {

    private final GithubFileDownloader githubFileDownloader;

    @Tool("Downloads build.gradle file from GitHub for the specified project")
    public String getGradleConfig(String projectName) {
        try {
            return githubFileDownloader.getFileContent(projectName, "development", "build.gradle");
        } catch (Exception e) {
            log.error("Error downloading file", e);
        }
        return null;
    }
}
