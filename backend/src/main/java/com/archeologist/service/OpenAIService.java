package com.archeologist.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIService.class);

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:nomic-embed-text}")
    private String ollamaModel;

    private OpenAiService openAiService;
    private final RestTemplate rest;

    public OpenAIService(@Value("${openai.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.rest = new RestTemplate();

        if (apiKey != null && !apiKey.isEmpty()) {
            try {
                this.openAiService = new OpenAiService(apiKey);
            } catch (Exception e) {
                logger.warn("Failed to initialize OpenAiService: {}", e.getMessage());
            }
        }
    }

    public List<Double> generateEmbedding(String text) {
        // Try Ollama first since it's local and free
        try {
            List<Double> ollamaEmbedding = generateEmbeddingWithOllama(text);
            if (ollamaEmbedding != null && !ollamaEmbedding.isEmpty()) {
                logger.debug("Successfully generated embedding using Ollama (size={})", ollamaEmbedding.size());
                return ollamaEmbedding;
            }
        } catch (Exception e) {
            logger.warn("Ollama embedding failed: {}", e.getMessage());
        }

        // Fallback to OpenAI if configured
        if (openAiService != null) {
            try {
                EmbeddingRequest request = EmbeddingRequest.builder()
                        .model("text-embedding-ada-002")
                        .input(List.of(text))
                        .build();

                List<Double> embedding = openAiService.createEmbeddings(request)
                        .getData()
                        .get(0)
                        .getEmbedding();

                logger.debug("Successfully generated embedding using OpenAI (size={})", embedding.size());
                return embedding;
            } catch (Exception e) {
                logger.error("Error generating OpenAI embedding: {}", e.getMessage());
            }
        }

        return Collections.emptyList();
    }

    public List<Double> generateEmbeddingWithOllama(String text) {
        String url = String.format("%s/api/embeddings", ollamaBaseUrl.replaceAll("/$", ""));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "model", ollamaModel,
                    "prompt", text
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = rest.exchange(url, HttpMethod.POST, request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("Ollama API call failed with status: " + response.getStatusCode());
            }

            Map<?, ?> responseBody = response.getBody();
            Object embeddingObj = responseBody.get("embedding");
            if (embeddingObj == null) {
                throw new RuntimeException("No embedding in response");
            }

            if (!(embeddingObj instanceof List)) {
                throw new RuntimeException("Unexpected embedding format");
            }

            List<?> raw = (List<?>) embeddingObj;
            List<Double> embedding = new ArrayList<>(raw.size());
            for (Object o : raw) {
                if (o instanceof Number) {
                    embedding.add(((Number) o).doubleValue());
                } else {
                    embedding.add(Double.parseDouble(o.toString()));
                }
            }

            return embedding;

        } catch (Exception e) {
            logger.error("Ollama embedding generation failed: {}", e.getMessage());
            throw new RuntimeException("Ollama embedding generation failed: " + e.getMessage(), e);
        }
    }

    public String generateCompletion(String prompt) {
        try {
            return generateCompletionWithOllama(prompt);
        } catch (Exception e) {
            logger.warn("Ollama completion failed: {}", e.getMessage());
        }

        if (openAiService != null) {
            try {
                ChatCompletionRequest request = ChatCompletionRequest.builder()
                        .model("gpt-4o-mini")
                        .messages(List.of(new ChatMessage(ChatMessageRole.USER.value(), prompt)))
                        .maxTokens(1000)
                        .build();

                return openAiService.createChatCompletion(request)
                        .getChoices()
                        .get(0)
                        .getMessage()
                        .getContent();

            } catch (Exception e) {
                logger.error("Error generating OpenAI completion: {}", e.getMessage());
            }
        }

        return "Unable to generate completion.";
    }

    public String generateCompletionWithOllama(String prompt) {
        String url = String.format("%s/api/generate", ollamaBaseUrl.replaceAll("/$", ""));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "model", "llama3.2", // Default model, can be configured
                    "prompt", prompt,
                    "stream", false
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = rest.exchange(url, HttpMethod.POST, request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("Ollama API call failed with status: " + response.getStatusCode());
            }

            Map<?, ?> responseBody = response.getBody();
            Object responseObj = responseBody.get("response");
            if (responseObj == null) {
                throw new RuntimeException("No response in Ollama result");
            }

            return responseObj.toString();

        } catch (Exception e) {
            logger.error("Ollama completion generation failed: {}", e.getMessage());
            throw new RuntimeException("Ollama completion generation failed: " + e.getMessage(), e);
        }
    }
}