package com.example.gradle_versions;

import com.example.gradle_versions.ai_tools.ApplicationConfigTool;
import com.example.gradle_versions.ai_tools.DependencyVersionCollectionTool;
import com.example.gradle_versions.ai_tools.GradleConfigTool;
import com.example.gradle_versions.aiservice.Assistant;
import com.example.gradle_versions.service.GithubFileDownloader;
import com.example.gradle_versions.utils.FileUtils;
import com.example.gradle_versions.utils.LlmFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.example.gradle_versions.utils.Prompts.DEPENDENCIES_SUMMARY;
import static com.example.gradle_versions.utils.Prompts.DEPENDENCIES_VERSIONS_PROMPT;


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
    private final FileUtils fileUtils;
    @Value("${internalDeps}")
    private String internalDeps;
    @Value("${projectsRootDir}")
    private String projectsRootDir;

    @Override
    public void run(String... args) throws Exception {

        if(args.length  != 1) {
            log.error("Please provide the quad name as an argument.");
            return;
        }

        StringBuilder reports = new StringBuilder();

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
            updateGit(project);
//            if(project.equalsIgnoreCase("post-processor")) {
//                continue; //this project has separate gradle.properties file, need to review
//            }

            // Check if the file already exists
            String projectDependenciesSummary = fileUtils.loadProjectReport(project);
            if (projectDependenciesSummary != null) {
                log.info("File already exists for project: " + project + ", skipping.");
                reports.append("\n\n\n**PROJECT: ")
                        .append(project)
                        .append(" **\n")
                        .append(projectDependenciesSummary);
                continue; // Skip to the next iteration
            }

            //TODO: files should be taken from separate directory, updating by git pull
            String gradleConfig = gradleConfigTool.getGradleConfigFromLocal(project);
//            String gradleConfig = gradleConfigTool.getGradleConfig(project);
            log.info("Summarizing deps for project: {}", project);
            try {
                projectDependenciesSummary = dockerAssistant.chat(DEPENDENCIES_VERSIONS_PROMPT
                        .replace("{context}", gradleConfig)
                        .replace("{internal-deps}", internalDeps));
                String projectDependenciesSummaryTable = extractTableContent(projectDependenciesSummary);
                if (projectDependenciesSummaryTable == null) {
                    log.error("No dependencies found for project: {}", project);

                    projectDependenciesSummary = awsAssistant.chat(DEPENDENCIES_VERSIONS_PROMPT.replace("{context}", gradleConfig));
                    projectDependenciesSummaryTable = extractTableContent(projectDependenciesSummary);
                }
                if (projectDependenciesSummaryTable != null) {
                    log.info("Dependencies found for project: {}", project);

                    // Save the content to a file
                    fileUtils.saveProjectReport(project, projectDependenciesSummaryTable);
                    reports.append("\n\n\n**PROJECT: ").append(project).append(" **\n").append(projectDependenciesSummaryTable);
                } else {
                    log.error("No dependencies found for project: {} after 2 runs, skipping", project);
                }
            } catch (Exception e) {
                log.error("Error processing project: {}", project, e);
            }
        }
        log.info("Generating final report!!");
//        String finalReport = awsAssistant.chat(DEPENDENCIES_SUMMARY.replace("{data}", reports));
        String prompt = DEPENDENCIES_SUMMARY.replace("{data}", reports.toString());
        saveFile("prompt", prompt);
        String finalReport = awsAssistant.chat(prompt);
//        System.out.println(finalReport);
        saveFile("final_report.html", finalReport);
        System.exit(0);
    }

    private void updateGit(String projectName) {

        if(fileUtils.reportPathExists()) {
            log.info("Report path exists, Git repos should be already updated today");
            return;
        }

        String projectPath = projectsRootDir + "/" + projectName;
        try {
            Process process = Runtime.getRuntime().exec(new String[] {"git", "-C", projectPath, "pull"});
            log.info("Executing git pull for project: {}", projectName);
            if (process.waitFor() == 0) {
                log.info("Git pull successful for project: {}", projectName);
            } else {
                log.error("Git pull failed for project: {}", projectName);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error executing git pull for project: {}", projectName, e);
        }
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
            log.error("Error saving file: {}", filename, e);
        }
    }
}
