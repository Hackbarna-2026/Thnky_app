package com.thnky.ai;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Talks HTTP to Nebius's OpenAI-compatible chat completions endpoint. Knows
 * nothing about challenges, prompts or grading — that lives in its callers.
 */
@Component
public class NebiusClient {

    private static final int MAX_TOKENS = 1000;

    private final RestClient restClient;
    private final String model;

    public NebiusClient(
            @Value("${thnky.nebius.base-url}") String baseUrl,
            @Value("${thnky.nebius.api-key}") String apiKey,
            @Value("${thnky.nebius.model}") String model,
            @Value("${thnky.nebius.timeout-seconds}") int timeoutSeconds
    ) {
        this.model = model;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    /** Returns the raw JSON content of the model's reply — the caller parses it into its own shape. */
    public String complete(List<ChatMessage> messages, String schemaName, JsonNode schema) {
        var request = new ChatCompletionRequest(
                model,
                messages,
                new ResponseFormat("json_schema", new JsonSchemaSpec(schemaName, true, schema)),
                MAX_TOKENS
        );

        ChatCompletionResponse response;
        try {
            response = restClient.post()
                    .uri("chat/completions")
                    .body(request)
                    .retrieve()
                    .body(ChatCompletionResponse.class);
        } catch (RestClientException e) {
            throw new NebiusRequestException("Nebius request failed", e);
        }

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new NebiusRequestException("Nebius returned no choices");
        }
        String content = response.choices().getFirst().message().content();
        if (content == null || content.isBlank()) {
            throw new NebiusRequestException("Nebius returned empty content");
        }
        return content;
    }

    private record ChatCompletionRequest(
            String model,
            List<ChatMessage> messages,
            @JsonProperty("response_format") ResponseFormat responseFormat,
            @JsonProperty("max_tokens") int maxTokens
    ) {
    }

    private record ResponseFormat(String type, @JsonProperty("json_schema") JsonSchemaSpec jsonSchema) {
    }

    private record JsonSchemaSpec(String name, boolean strict, JsonNode schema) {
    }

    private record ChatCompletionResponse(List<ChatChoice> choices) {
    }

    private record ChatChoice(ChatResponseMessage message) {
    }

    private record ChatResponseMessage(String content) {
    }
}
