package com.example.dependency_version_collector.config;

import com.example.dependency_version_collector.service.MyChatModelListener;
import com.example.dependency_version_collector.utils.LlmFactory;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Configuration
public class AiConfig {

    @Bean
    public ChatModel chatModel(LlmFactory llmFactory) {
        return llmFactory.get(LlmFactory.ModelType.AWS);
    }

    @Bean
    ChatModelListener chatModelListener() {
        return new MyChatModelListener();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(10);
    }
}
