package com.example.gradle_versions.utils;

import dev.langchain4j.http.client.jdk.JdkHttpClient;
import dev.langchain4j.model.bedrock.BedrockChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

@Component
public class LlmFactory {

    private static final Collection<ModelParameters> MODELS = List.of(
            new ModelParameters("ai/mistral:latest", ModelType.DOCKER, "http://127.0.0.1:12434/engines/v1", 0), //good
            new ModelParameters("ai/mistral-nemo:latest", ModelType.DOCKER, "http://127.0.0.1:12434/engines/v1", 0),
            new ModelParameters("ai/gemma3:latest", ModelType.DOCKER, "http://127.0.0.1:12434/engines/v1", 0),
            new ModelParameters("ai/deepseek-r1-distill-llama:8B-Q4_0", ModelType.DOCKER, "http://127.0.0.1:12434/engines/v1", 0),
            new ModelParameters("DeepSeek-R1-Distill-Qwen-7B-Q4_K_M-local.gguf:latest", ModelType.OLLAMA, "http://127.0.0.1:11434", 32768),
            new ModelParameters("Qwen2.5-7B-Instruct-1M-Q6_K.gguf:latest", ModelType.OLLAMA, "http://127.0.0.1:11434", 32768),
            new ModelParameters("Qwen2.5.1-Coder-7B-Instruct-Q4_K_M-local.gguf:latest", ModelType.OLLAMA, "http://127.0.0.1:11434", 32768)
    );
    public static final Duration TIMEOUT = Duration.of(30, ChronoUnit.MINUTES);

    private static String DEFAULT_DOCKER_MODEL_NAME = "ai/deepseek-r1-distill-llama:8B-Q4_0"; //"ai/gemma3:latest" - bad; // "ai/mistral:latest";
    private static String DEFAULT_OLLAMA_MODEL_NAME = "local/qwen2_5:latest";

    public ChatModel get(ModelType modelType) {
        return switch (modelType) {
            case DOCKER -> initDockerModel();
            case OLLAMA -> initOllamaModel();
            case AWS -> initAwsModel();
        };
    }

    private ChatModel initDockerModel() {
        ModelParameters modelParameters = MODELS.stream()
                .filter(model -> model.modelType() == ModelType.DOCKER)
                .filter(model -> model.modelName().equals(DEFAULT_DOCKER_MODEL_NAME))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No Docker model found"));

        return OpenAiChatModel.builder()
                .baseUrl(modelParameters.baseUrl())
                .modelName(modelParameters.modelName())
                .logRequests(true)
                .logResponses(true)
                .timeout(TIMEOUT)
                .httpClientBuilder(JdkHttpClient.builder().connectTimeout(TIMEOUT).readTimeout(TIMEOUT))
                .build();
    }
    private ChatModel initOllamaModel() {
        ModelParameters modelParameters = MODELS.stream()
                .filter(model -> model.modelType() == ModelType.DOCKER)
                .filter(model -> model.modelName().equals(DEFAULT_OLLAMA_MODEL_NAME))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No Ollama model found"));

        return OllamaChatModel.builder()
                .baseUrl(modelParameters.baseUrl())
                .modelName(modelParameters.modelName())
                .logRequests(true)
                .logResponses(true)
                .timeout(TIMEOUT)
                .httpClientBuilder(JdkHttpClient.builder().connectTimeout(TIMEOUT).readTimeout(TIMEOUT))
                .numCtx(modelParameters.numCtx())
                .build();
    }
    private ChatModel initAwsModel() {
        return BedrockChatModel.builder()
//                .modelId("eu.amazon.nova-pro-v1:0")
                .modelId("eu.amazon.nova-lite-v1:0")
                .region(Region.EU_WEST_1)
                .timeout(TIMEOUT)
                .defaultRequestParameters(
                        ChatRequestParameters.builder()
                                .temperature(0.0)
                                .maxOutputTokens(100000)
                                .topP(1.0)
                                .build()
                )
                .build();
    }

    public enum ModelType {
        DOCKER,
        OLLAMA,
        AWS
    }

    private record ModelParameters(String modelName, ModelType modelType, String baseUrl, int numCtx) {
    }
}
