package com.example.gradle_versions.aiservice;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

import java.util.List;

@AiService
public interface Assistant {

    @SystemMessage("You are experienced and precise assistant. Respond with retrieved data only, not adding any additional information, like numeration or headings.")
    List<String> chatToList(String userMessage);

    @SystemMessage("You are experienced Software Engineer with extensive experience in Java and Spring Boot. Respond precisely without any explanations or thought process.")
//    @SystemMessage("You are experienced and precise assistant")
    String chat(String userMessage);
}
