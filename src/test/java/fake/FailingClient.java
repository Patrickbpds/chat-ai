package fake;

import com.patrick.domain.Message;
import com.patrick.infra.gemini.AiClient;

import java.io.IOException;
import java.util.List;

public class FailingClient implements AiClient {
    @Override
    public String generate (String modelId, List<Message> history, String userInput, String systemPrompt) throws IOException {
        throw new IOException("API error");
    }
}
