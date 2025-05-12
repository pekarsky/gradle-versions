package com.example.dependency_version_collector.aiservice;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

import java.util.List;

@AiService
public interface Assistant {

    @SystemMessage("You are experienced and precise assistant. Respond with retrieved data only, not adding any additional information, like numeration or headings.")
    List<String> chatToList(String userMessage);

    @SystemMessage("You are experienced and precise assistant")
    String chat(String userMessage);
}
