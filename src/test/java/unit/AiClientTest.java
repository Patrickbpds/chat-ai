package unit;

import com.patrick.domain.Message;
import com.patrick.infra.gemini.AiClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AiClientTest {

    private static class FakeAiClient implements AiClient {
        @Override
        public String generate(String modelId, List<Message> history, String userInput, String systemPrompt) throws IOException {

            if (modelId == null) throw new IllegalArgumentException("modelId cannot be null");
            if (history == null) throw new IllegalArgumentException("history cannot be null");
            if (userInput == null) throw new IllegalArgumentException("userInput cannot be null");

            return "Fake response for: " + userInput;
        }
    }

        @Test
        public void shouldGenerateResponseWithFakeImplementation() throws IOException {
            AiClient aiClient = new FakeAiClient();
            List<Message> history = new ArrayList<>();
            String response = aiClient.generate("fake-model", history, "How are you?", "You are a helpful assistant.");

            assert response.equals("Fake response for: How are you?");
        }

        @Test
        public void shouldValidateRequiredParameters() {
            AiClient aiClient = new FakeAiClient();
            List<Message> history = new ArrayList<>();

            assertThrows(IllegalArgumentException.class, () -> {
                aiClient.generate(null, history, "Hello", "");
            });

            assertThrows(IllegalArgumentException.class, () -> {
                aiClient.generate("gemini", null, "Hello", "");
            });

            assertThrows(IllegalArgumentException.class, () -> {
                aiClient.generate("gemini", history, null, "");
            });
        }
    }
