package com.example.gradle_versions;

import com.example.gradle_versions.ai_tools.ApplicationConfigTool;
import com.example.gradle_versions.ai_tools.DependencyVersionCollectionTool;
import com.example.gradle_versions.ai_tools.GradleConfigTool;
import com.example.gradle_versions.aiservice.Assistant;


import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.example.gradle_versions.utils.Prompts.DEPENDENCIES_SUMMARY;
import static com.example.gradle_versions.utils.Prompts.DEPENDENCIES_VERSIONS_PROMPT;


@Component
@RequiredArgsConstructor
@Slf4j
public class Runner
        // implements CommandLineRunner
    // runner intended to be more agentic - t opass tools choice to LLM
{
    private final ChatModel chatModel;
    private final DependencyVersionCollectionTool dependencyVersionCollectionTool;
    private final ApplicationConfigTool applicationConfigTool;
    private final GradleConfigTool gradleConfigTool;
    @Value("${internalDeps}")
    private String internalDeps;

//    @Override
    public void run(String... args) throws Exception {

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .tools(List.of(applicationConfigTool))
                .build();

        String p = assistant.chat("Please, collect list of projects, that belongs to squad 'SQUAD_HERE'. " +
                "You can use tools you've already used. " +
                "Please, provide the list in format: 'project1, project2, project3'");
        List<String> projects = assistant.chatToList("Extract project names from following data, remove heading and numeration: " + p);
        String reports = "";

        assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .tools(List.of(dependencyVersionCollectionTool))
                .build();

        for(String project : projects) {
            Path path = Paths.get(project + ".html");
            // Check if the file already exists
            if (Files.exists(path)) {
                log.info("File already exists for project: " + project + ", skipping.");
                reports += "\n\n\n**PROJECT: " + project + " **\n" + Files.readString(path);
                continue; // Skip to the next iteration
            }

            //TODO: files should be taken from separate directory, updating by git pull
            String gradleConfig = gradleConfigTool.getGradleConfig(project);
            log.info("Summarizing deps for project: " + project);
            try {
                String projectDependenciesSummary = assistant.chat(DEPENDENCIES_VERSIONS_PROMPT
                        .replace("{context}", gradleConfig)
                        .replace("{internal-deps}", internalDeps)
                );
                projectDependenciesSummary = extractTableContent(projectDependenciesSummary);
                if (projectDependenciesSummary == null) {
                    log.error("No dependencies found for project: " + project);
                    projectDependenciesSummary = extractTableContent(assistant.chat(DEPENDENCIES_VERSIONS_PROMPT.replace("{context}", gradleConfig).replace("{internal-deps}",
                            internalDeps)));
                }
                if (projectDependenciesSummary != null) {
                    log.info("Dependencies found for project: " + project);

                    // Save the content to a file
                    try {
                        Files.write(path, projectDependenciesSummary.getBytes());
                        log.info("Saved dependencies summary to file: " + path);
                    } catch (IOException e) {
                        log.error("Error saving dependencies summary to file: " + path, e);
                    }

                    reports += "\n\n\n**PROJECT: " + project + " **\n" + projectDependenciesSummary;
                } else {
                    log.error("No dependencies found for project: " + project + " after 2 runs, skipping");
                }
            } catch (Exception e) {
                log.error("Error processing project: " + project, e);
            }
        }
        log.info("Generating final report!!");
        String finalReport = assistant.chat(DEPENDENCIES_SUMMARY.replace("{data}", reports));
        System.out.println(finalReport);
        System.exit(0);
    }

    public static String extractTableContent(String response) {
        int startIndex = response.indexOf("<table>");
        int endIndex = response.indexOf("</table>");
        if (startIndex != -1 && endIndex != -1) {
            return response.substring(startIndex, endIndex + 8); // +7 to skip "<table>"
        }
        return null; // Return empty string if <table> or </table> is not found
    }
}
