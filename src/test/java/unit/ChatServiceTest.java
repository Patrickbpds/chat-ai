package unit;

import com.patrick.application.ChatService;
import com.patrick.domain.Message;
import com.patrick.domain.Role;
import fake.FailingClient;
import fake.FakeClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChatServiceTest {
    @Test
    public void replyAddsUserAndModelMessages() throws IOException {
        ChatService chatService = new ChatService(
                new FakeClient(), "gemini-2.5-flash", "You are a helpful assistant.", 5);
        List<Message> history = new ArrayList<>();

        String answer = chatService.reply(history, "Hello, how are you?");

        assertEquals("eco: Hello, how are you?", answer);
        assertEquals(2, history.size());
        assertEquals(Role.USER, history.get(0).role());
        assertEquals(Role.MODEL, history.get(1).role());
    }

    @Test
    public void constructorValidation(){
        assertThrows(NullPointerException.class, () -> new ChatService(null, "model", "system prompt", 5));
        assertThrows(NullPointerException.class, () -> new ChatService(new FakeClient(), null, "system prompt", 5));
        assertDoesNotThrow(() -> new ChatService(new FakeClient(), "model", null, 5));
        ChatService svc = new ChatService(new FakeClient(), "model", "system prompt", 1);
        assertDoesNotThrow(()-> svc.reply(new ArrayList<>(), "test"));
    }

    @Test
    public void replyValidation() {
        ChatService svc = new ChatService(
                new FakeClient(), "gemini-2.5-flash", "You are a helpful assistant.", 5);
        assertThrows(NullPointerException.class, () -> svc.reply(null, "Input"));
        assertThrows(NullPointerException.class, () -> svc.reply(new ArrayList<>(), null));
    }

    @Test
    public void propagatesIOException() {
        ChatService svc = new ChatService(
                new FailingClient(), "gemini-2.5-flash", "You are a helpful assistant.", 5);
        List<Message> history = new ArrayList<>();

        IOException ex = assertThrows(IOException.class, () -> svc.reply(history, "input"));
        assertEquals("API error", ex.getMessage());
        assertEquals(0, history.size());
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "1, 1",
            "2, 2",
            "4, 4",
            "6, 4",
            "8, 6",
            "3, 3"
    })
    public void trimHistoryBoundaries(int historySize, int expectedSize) {
        List<Message> history = new ArrayList<>();
        for (int i = 0; i < historySize; i++) {
            Role role = (i % 2 == 0) ? Role.USER : Role.MODEL;
            history.add(new Message(role, "Message " + i));
        }

        List<Message> trimmed = ChatService.trim(history, 2);
        assertEquals(expectedSize, trimmed.size());

        if (trimmed.size() > 0) {
            assertEquals(history.get(history.size() - 1).text(),
                    trimmed.get(trimmed.size() - 1).text());
        }
    }

    @Test
    public void trimWithMixedRoles() {
        List<Message> history = List.of(
                new Message(Role.MODEL, "genesis"),
                new Message(Role.USER, "User 1"),
                new Message(Role.MODEL, "Model 1")
        );

        List<Message> trimmed = ChatService.trim(history, 2);
        assertEquals(3, trimmed.size());
        assertEquals(Role.MODEL, trimmed.get(0).role());
        assertEquals(Role.USER, trimmed.get(1).role());
        assertEquals(Role.MODEL, trimmed.get(2).role());
    }
}
