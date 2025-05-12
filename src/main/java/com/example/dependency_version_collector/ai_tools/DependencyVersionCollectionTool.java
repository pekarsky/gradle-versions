package com.example.dependency_version_collector.ai_tools;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.example.dependency_version_collector.utils.Prompts.DEPENDENCIES_VERSIONS_PROMPT;

@Component
@RequiredArgsConstructor
@Slf4j
public class DependencyVersionCollectionTool {

    private final ChatModel chatModel;

    @Tool("Collecting dependencies versions from build.gradle file")
    public String getDependenciesVersion(String gradleFile) {
        PromptTemplate promptTemplate = PromptTemplate.from(DEPENDENCIES_VERSIONS_PROMPT);
        Map<String, Object> variables = Map.of("context", gradleFile);
        Prompt prompt = promptTemplate.apply(variables);

        String response = chatModel.chat(prompt.text());
        return response;
    }
}
