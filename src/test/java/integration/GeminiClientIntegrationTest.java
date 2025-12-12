package integration;

import com.patrick.domain.Message;
import com.patrick.domain.Role;
import com.patrick.infra.gemini.GeminiClient;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GeminiClientIntegrationTest {

    MockWebServer server;
    GeminiClient client;

    @BeforeEach
    public void setup() throws IOException {
        server = new MockWebServer();
        server.start();

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(5))
                .build();
        String base = "http://127.0.0.1:" + server.getPort();
        client = new GeminiClient(httpClient, base, "test-api");
    }

    @AfterEach
    public void teardown() throws IOException {
        server.shutdown();
    }

    @Test
    public void successParsesCandidates() throws Exception{

        String body = "{ \"candidates\": [ { \"content\": { \"parts\":  [{\"text\": \"Hello!\"} ]}} ] }";
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(body)
                .addHeader("Content-Type", "application/json")
        );
        String output = client.generate(
                "gemini-2.5-flash",
                List.of(new Message(Role.USER, "Hello")),
                "How are you?",
                "SystemPrompt"
        );
        assertTrue(output.contains("Hello"));
        var recorded = server.takeRequest();
        assertTrue(recorded.getPath().contains(":generateContent?key=test-api"));
        assertEquals("POST", recorded.getMethod());
    }

    @Test
    public void retriesOn500() {
        server.enqueue(new MockResponse().setResponseCode(500).setBody("{}"));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"candidates\": [ { \"content\": { \"parts\":  [{\"text\": \"OK\"} ]}} ] }")
        );

        assertDoesNotThrow(() -> client.generate(
                    "gemini-2.5-flash",
                    List.of(),
                    "Hi",
                    "sys")
        );
    }

    @Test
    public void failsOn401(){
        server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"error\": \"Unauthorized\"}"));

        Exception exception = assertThrows(IOException.class, () -> client.generate(
                    "gemini-2.5-flash",
                    List.of(),
                    "Hi",
                    "sys")
        );
    }

    @Test
    public void failsOn429(){
        server.enqueue(new MockResponse().setResponseCode(429).setBody("{\"error\": \"Rate limit exceeded\"}"));
        server.enqueue(new MockResponse().setResponseCode(200).setBody(
                "{ \"candidates\": [ { \"content\": { \"parts\":  [{\"text\": \"Retry success\"} ]}} ] }"));

        assertDoesNotThrow(() -> client.generate(
                    "gemini-2.5-flash",
                    List.of(),
                    "test",
                    "sys")
        );
    }

    @Test
    public void constructorValidation() {
        OkHttpClient http = new OkHttpClient();
        assertThrows(NullPointerException.class, () ->
                new GeminiClient(null, "http://test.com", "key")
        );
        assertThrows(NullPointerException.class, () ->
                new GeminiClient(http, null, "key")
        );
        assertThrows(NullPointerException.class, () ->
                new GeminiClient(http, "http://test.com", null)
        );
    }

    @Test
    public void generateValidation() {
        assertThrows(IllegalArgumentException.class, () ->
                client.generate(null, List.of(), "input", "sys")
        );
        assertThrows(IllegalArgumentException.class, () ->
                client.generate("model", null, "input", "sys")
        );
        assertThrows(IllegalArgumentException.class, () ->
                client.generate("model", List.of(), null, "sys")
        );
        assertDoesNotThrow(() -> {
         server.enqueue(new MockResponse().setResponseCode(200).setBody(
                "{ \"candidates\": [ { \"content\": { \"parts\":  [{\"text\": \"Hi\"} ]}} ] }"));
            client.generate("model", List.of(), "input", null);
        });
    }

    @Test
    public void handlesEmptyResponse() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}")
        );

        assertDoesNotThrow(() -> {
            String result = client.generate(
                    "model",
                    List.of(),
                    "input",
                    "sys"
            );
            assertEquals(   "[no candidates]", result);
        });
    }

    @Test
    public void handlesEmptyParts() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"candidates\": [ { \"content\": { \"parts\": [] }} ] }")
        );

        assertDoesNotThrow(() -> {
            String result = client.generate(
                    "model",
                    List.of(),
                    "input",
                    "sys"
            );
            assertEquals(   "[empty]", result);
        });
    }

    @Test
    public void handlesEmptyText(){
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"candidates\": [ { \"content\": { \"parts\": [ {\"text\": \"\"} ] }} ] }")
        );

        assertDoesNotThrow(() -> {
            String result = client.generate(
                    "model",
                    List.of(),
                    "input",
                    "sys"
            );
            assertEquals(   "[empty]", result);
        });
    }

    @Test
    public void handlesMultipleParts(){
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"candidates\": [ { \"content\": { \"parts\": [ {\"text\": \"Hello \"}, {\"text\": \"World!\"} ] }} ] }")
        );

        assertDoesNotThrow(() -> {
            String result = client.generate(
                    "model",
                    List.of(),
                    "input",
                    "sys"
            );
            assertEquals("Hello World!", result);
        });
    }

    @Test
    public void buildsCorrectPayloadWithHistory() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"candidates\": [ { \"content\": { \"parts\": [ {\"text\": \"Response\"} ] }} ] }")
        );

        List<Message> history = List.of(
                new Message(Role.USER, "Hi"),
                new Message(Role.MODEL, "Hello!")
        );
        client.generate(
                "gemini-2.5-flash",
                history,
                "How are you?",
                "Be helpful."
        );

        var request = server.takeRequest();
        String body = request.getBody().readUtf8();

        assertTrue(body.contains("systemInstruction"));
        assertTrue(body.contains("Be helpful."));

        assertTrue(body.contains("contents"));

        assertTrue(body.contains("\"role\":\"user\""));
        assertTrue(body.contains("\"role\":\"model\""));

        assertTrue(body.contains("\"text\":\"Hi\""));
        assertTrue(body.contains("\"text\":\"Hello!\""));
        assertTrue(body.contains("\"text\":\"How are you?\""));
    }

    @Test
    public void handlesBlankSystemPrompt() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"candidates\": [ { \"content\": { \"parts\": [ {\"text\": \"OK\"} ] }} ] }")
        );

        client.generate(
                "gemini-2.5-flash",
                List.of(),
                "input",
                ""
        );

        var request = server.takeRequest();
        String body = request.getBody().readUtf8();

        assertFalse(body.contains("systemInstruction"));
    }
}