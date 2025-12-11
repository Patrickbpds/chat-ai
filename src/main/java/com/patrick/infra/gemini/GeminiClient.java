package com.patrick.infra.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.patrick.domain.Message;
import com.patrick.domain.Role;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class GeminiClient implements AiClient {
    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    private static final Logger LOG = Logger.getLogger(GeminiClient.class.getName());
    private final OkHttpClient httpClient;
    private static final int MAX_RETRIES = 3;
    private IOException last;
    private final String baseURL;
    private final String apiKey;
    private final ObjectMapper mapper = new ObjectMapper();

    public GeminiClient(OkHttpClient http, String baseURL, String apiKey) {
        this.httpClient = Objects.requireNonNull(http);
        this.baseURL = Objects.requireNonNull(baseURL);
        this.apiKey = Objects.requireNonNull(apiKey);
    }

    @Override
    public String generate(String modelId, List<Message> history, String userInput, String systemPrompt) throws IOException {
        if (modelId == null) throw new IllegalArgumentException("modelId cannot be null");
        if (history == null) throw new IllegalArgumentException("history cannot be null");
        if (userInput == null) throw new IllegalArgumentException("userInput cannot be null");
        Objects.requireNonNull(modelId);
        Objects.requireNonNull(history);
        Objects.requireNonNull(userInput);

        Map<String, Object> payload = new LinkedHashMap<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            Map<String, Object> system = new LinkedHashMap<>();
            system.put("role", Role.SYSTEM.Wire());
            system.put("parts", List.of(Map.of("text", systemPrompt)));
            payload.put("systemInstruction", system);
        }

        List<Map<String, Object>> contents = new ArrayList<>();
        for (Message msg : history) {
            contents.add(
                    Map.of(
                            "role", msg.role().Wire(),
                            "parts", List.of(Map.of("text", msg.text()))
                    )
            );
        }

        contents.add(Map.of(
                "role", Role.USER.Wire(),
                "parts", List.of(Map.of("text", userInput))
        ));

        payload.put("contents", contents);

        String url = String.format("%s/v1beta/models/%s:generateContent?key=%s",
                baseURL,
                modelId,
                apiKey
        );
        RequestBody body = RequestBody.create(
                mapper.writeValueAsString(payload),
                JSON
        );
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Content-Type", "application/json")
                .build();

        return executeWithRetry(request);
    }

    private String executeWithRetry(Request request) throws IOException {
        long backoffMs = 250;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            long start = System.nanoTime();

            try (Response response = httpClient.newCall(request).execute()) {
                long tookMs = Duration.ofNanos(System.nanoTime() - start).toMillis();
                int code = response.code();
                String respBody = response.body() != null ? response.body().string() : "";
                LOG.info(String.format("GeminiClient request took %d ms, attempt %d, response code %d",
                        tookMs, attempt, code));
                if (code >= 200 && code < 300) {
                    return parseText(respBody);
                }

                if (code == 429 || code >= 500) {
                    last = new IOException("Retryable status code: " + code + ", body: " + respBody);
                } else {
                    throw new IOException("Non-retryable status code: " + code + ", body: " + respBody);
                }
            } catch (IOException e) {
                last = e;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(backoffMs);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted during backoff", ie);
            }
            backoffMs *= 2;
        }
        throw new IOException("Failed after " + MAX_RETRIES + " attempts", last);
    }

    private String parseText(String body) throws IOException {
        JsonNode root = mapper.readTree(body);
        JsonNode candidates = root.get("candidates");
        if (candidates != null && candidates.isArray() && candidates.size() > 0) {
            JsonNode content = candidates.get(0).get("content");
            if (content != null) {
                JsonNode parts = content.get("parts");
                if (parts != null && parts.isArray()) {
                    if (parts.size() == 0) {
                        return "[empty]";
                    }
                    StringBuilder sb = new StringBuilder();
                    for (JsonNode p : parts) {
                        JsonNode t = p.get("text");
                        if (t != null)
                            sb.append(t.asText());
                        }
                        return sb.toString().isBlank() ? "[empty]" : sb.toString();
                    }
                }
            }
        return "[no candidates]";
    }
}
