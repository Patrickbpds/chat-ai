package com.patrick;

import com.patrick.infra.config.Env;
import okhttp3.OkHttpClient;

import java.time.Duration;

public class Main {
    public static void main(String[] args) throws Exception {
      String apikey = Env.get("GOOGLE_API_KEY_GEMINI", true);
      String modelId = Env.getOrDefault("MODEL_ID", "gemini-1.5-flash");

      OkHttpClient httpClient = new OkHttpClient.Builder()
              .callTimeout(Duration.ofSeconds(30))
              .build();

        GeminiClient geminiClient = new GeminiClient(httpClient, "https://generativelanguage.googleapis.com", apikey);

        String system = "Write a short poem about the sea in English.";

        ChatService chatService = new ChatService(geminiClient, modelId, system, 5);

    }
}