package com.patrick.infra.gemini;

import com.patrick.domain.Message;
import java.io.IOException;
import java.util.List;

public interface AiClient {
    String generate(String modelId, List<Message> history, String userInput, String systemPrompt) throws IOException;
}
