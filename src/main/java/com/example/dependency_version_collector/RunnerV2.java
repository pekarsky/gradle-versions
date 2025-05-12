package com.example.dependency_version_collector;

import com.example.dependency_version_collector.ai_tools.ApplicationConfigTool;
import com.example.dependency_version_collector.ai_tools.DependencyVersionCollectionTool;
import com.example.dependency_version_collector.ai_tools.GradleConfigTool;
import com.example.dependency_version_collector.aiservice.Assistant;
import com.example.dependency_version_collector.service.GithubFileDownloader;
import com.example.dependency_version_collector.utils.LlmFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.bedrock.endpoints.internal.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.example.dependency_version_collector.utils.Prompts.DEPENDENCIES_SUMMARY;
import static com.example.dependency_version_collector.utils.Prompts.DEPENDENCIES_VERSIONS_PROMPT;


@Component
@RequiredArgsConstructor
@Slf4j
public class RunnerV2 implements CommandLineRunner {
    private final ChatModel chatModel;
    private final LlmFactory llmFactory;
    private final DependencyVersionCollectionTool dependencyVersionCollectionTool;
    private final ApplicationConfigTool applicationConfigTool;
    private final GradleConfigTool gradleConfigTool;
    private final GithubFileDownloader githubFileDownloader;
    private final String innerDeps = System.getenv("INNER_DEPS");

    @Override
    public void run(String... args) throws Exception {

        if(args.length  != 1) {
            log.error("Please provide the quad name as an argument.");
            return;
        }

        String reports = "";

        Assistant dockerAssistant = AiServices.builder(Assistant.class)
                .chatModel(llmFactory.get(LlmFactory.ModelType.DOCKER))
                .tools(List.of(dependencyVersionCollectionTool))
                .build();
        Assistant awsAssistant = AiServices.builder(Assistant.class)
                .chatModel(llmFactory.get(LlmFactory.ModelType.AWS))
                .tools(List.of(dependencyVersionCollectionTool))
                .build();


//        String appConfigFileContent = githubFileDownloader.getFileContent("concourse-pipelines", "master", "config/apps.yml");
//        String projectResponse = awsAssistant.chat(GET_PROJECTS_BY_SQUAD
//                .replace("{{context}}", appConfigFileContent)
//                .replace("{{squad}}", args[0])
//        );


        List<String> projects = applicationConfigTool.applicationsByOwnerSquad(args[0]);
        for(String project : projects) {
            if(project.equalsIgnoreCase("post-processor")) {
                continue; //this project has separate gradle.properties file, need to review
            }
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
                String projectDependenciesSummary = dockerAssistant.chat(
                        DEPENDENCIES_VERSIONS_PROMPT.replace("{context}", gradleConfig).replace("{internal-deps}", innerDeps));
                String projectDependenciesSummaryTable = extractTableContent(projectDependenciesSummary);
                if (projectDependenciesSummaryTable == null) {
                    log.error("No dependencies found for project: " + project);

                    projectDependenciesSummary = awsAssistant.chat(DEPENDENCIES_VERSIONS_PROMPT.replace("{context}", gradleConfig));
                    projectDependenciesSummaryTable = extractTableContent(projectDependenciesSummary);
                }
                if (projectDependenciesSummaryTable != null) {
                    log.info("Dependencies found for project: " + project);

                    // Save the content to a file
                    try {
                        Files.write(path, projectDependenciesSummaryTable.getBytes());
                        log.info("Saved dependencies summary to file: " + path);
                    } catch (IOException e) {
                        log.error("Error saving dependencies summary to file: " + path, e);
                    }

                    reports += "\n\n\n**PROJECT: " + project + " **\n" + projectDependenciesSummaryTable;
                } else {
                    log.error("No dependencies found for project: " + project + " after 2 runs, skipping");
                }
            } catch (Exception e) {
                log.error("Error processing project: " + project, e);
            }
        }
        log.info("Generating final report!!");
//        String finalReport = awsAssistant.chat(DEPENDENCIES_SUMMARY.replace("{data}", reports));
        String prompt = DEPENDENCIES_SUMMARY.replace("{data}", reports);
        saveFile("prompt", prompt);
        String finalReport = awsAssistant.chat(prompt);
//        System.out.println(finalReport);
        saveFile("final_report.html", finalReport);
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

    private void saveFile(String filename, String content) {
        try {
            Files.writeString(Paths.get(filename), content);
        } catch (IOException e) {
            log.error("Error saving file: " + filename, e);
        }
    }
}
