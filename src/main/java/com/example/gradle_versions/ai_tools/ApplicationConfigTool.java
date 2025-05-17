package com.example.gradle_versions.ai_tools;

import com.example.gradle_versions.model.AppConfig;
import com.example.gradle_versions.model.AppConfigWrapper;
import com.example.gradle_versions.service.GithubFileDownloader;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Log4j
public class ApplicationConfigTool {

    private final GithubFileDownloader githubFileDownloader;

    @Tool("Returns the list of projects that belong to the specified squad")
    public List<String> applicationsByOwnerSquad(String squad) {
        log.info("Fetching applications for squad: " + squad);
        try {
            String fileContent = githubFileDownloader.getFileContent("concourse-pipelines", "master", "config/apps.yml");
            List<AppConfig> appConfigs = parseYaml(fileContent);
            return appConfigs.stream().filter(a -> squad.equalsIgnoreCase(a.getOwner_team())).map(AppConfig::getName).toList();
        } catch (Exception e) {
            log.error("Error downloading or parsing the file", e);
            return Collections.EMPTY_LIST;
        }
    }

    private List<AppConfig> parseYaml(String yamlContent) {
        LoaderOptions loaderOptions = new LoaderOptions();
        Constructor constructor = new Constructor(AppConfigWrapper.class, loaderOptions);

        // Configure PropertyUtils to ignore unknown properties
        PropertyUtils propertyUtils = new PropertyUtils();
        propertyUtils.setSkipMissingProperties(true);
        constructor.setPropertyUtils(propertyUtils);

        Yaml yaml = new Yaml(constructor);
        AppConfigWrapper wrapper = yaml.load(yamlContent);
        return wrapper.getApps();
    }
}
