package com.patrick.application;

import com.patrick.domain.Message;
import com.patrick.domain.Role;
import com.patrick.infra.gemini.AiClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatService {
    private final AiClient client;
    private final String modelId;
    private final String systemPrompt;
    private final int maxTurns;

    public ChatService(AiClient client, String modelId, String systemPrompt, int maxTurns) {
        this.client = Objects.requireNonNull(client);
        this.modelId = Objects.requireNonNull(modelId);
        this.systemPrompt = systemPrompt == null ? "" : systemPrompt;
        this.maxTurns = Math.max(2, maxTurns);
    }

    public String reply(List<Message> history, String userInput) throws IOException {
        Objects.requireNonNull(history);
        Objects.requireNonNull(userInput);

        List<Message> trimmed = trim(history, maxTurns);
        String answer = client.generate(modelId, trimmed, userInput, systemPrompt);

        history.add(new Message(Role.USER, userInput));
        history.add(new Message(Role.MODEL, answer));
        return answer;
    }

    public static List<Message> trim(List<Message> history, int maxTurns) {
        if (history.isEmpty()){
            return new ArrayList<>(history);
        }
        List<Message> copyList = new ArrayList<>(history);

        List<Integer> pairEndIndexes = new ArrayList<>();
        for (int position = copyList.size() - 1; position > 0; position --) {
            if (copyList.get(position).role() == Role.MODEL && copyList.get(position - 1).role() == Role.USER) {
                pairEndIndexes.add(position);
            }
        }
        if (pairEndIndexes.size() <= maxTurns) {
            return copyList;
        }
        int keepFromIndex = pairEndIndexes.get(pairEndIndexes.size() - maxTurns) - 1;
        return copyList.subList(keepFromIndex, copyList.size());
    }
}
