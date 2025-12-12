package com.patrick;

import com.patrick.application.ChatService;
import com.patrick.domain.Message;
import com.patrick.infra.config.Env;
import com.patrick.infra.gemini.GeminiClient;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
      String apikey = Env.get("GOOGLE_API_KEY_GEMINI", true);
      String modelId = Env.getOrDefault("MODEL_ID", "gemini-2.5-flash");

      OkHttpClient httpClient = new OkHttpClient.Builder()
              .callTimeout(Duration.ofSeconds(30))
              .build();

        GeminiClient geminiClient = new GeminiClient(httpClient, "https://generativelanguage.googleapis.com", apikey);
        String system = "You are a specialized english tutor. Help the user learn English by answering their questions and providing explanations.";
        ChatService chatService = new ChatService(geminiClient, modelId, system, 5);

        run (chatService, System.in, System.out);
    }

    public static void run(ChatService service, InputStream in, PrintStream out) throws IOException{
        List<Message> history = new ArrayList<>();
        out.println("Welcome to the Chat Service! Type 'exit' to quit.");

        try (Scanner scanner = new Scanner(in)) {
            while (true) {
                out.print("You: ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String line = scanner.nextLine().trim();
                if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                    out.println("Goodbye!");
                    break;
                }
                if (line.isBlank()) continue;
                String answer = service.reply(history, line);
                out.println("Gemini: " + answer);
            }
        }
    }
}