# Chat AI - Gemini Integration

A Java-based chat application that integrates with Google's Gemini API, built using Test-Driven Development (TDD) principles. This project serves as a practical exercise in implementing TDD while working with external APIs.

## 🎯 Project Goal

This project was created to practice Test-Driven Development by implementing a complete Gemini API integration from scratch. Every feature was built following the red-green-refactor cycle, with tests written before implementation.

## ✨ Features

- **Conversational AI Chat**: Interactive command-line chat interface powered by Gemini
- **Conversation History Management**: Automatically maintains and trims conversation history
- **Configurable Context Window**: Control how many conversation turns to keep in context
- **Custom System Prompts**: Define the AI's behavior and personality
- **Robust Error Handling**: Automatic retry logic for transient failures
- **Comprehensive Test Coverage**: 80%+ line coverage, 70%+ branch coverage

## 🏗️ Architecture

The project follows Clean Architecture principles with clear separation of concerns:

```
src/
├── main/java/com/patrick/
│   ├── Main.java                          # Application entry point
│   ├── application/
│   │   └── ChatService.java               # Core business logic
│   ├── domain/
│   │   ├── Message.java                   # Domain model
│   │   └── Role.java                      # User/Model/System roles
│   └── infra/
│       ├── config/
│       │   └── Env.java                   # Environment configuration
│       └── gemini/
│           ├── AiClient.java              # AI client interface
│           └── GeminiClient.java          # Gemini API implementation
└── test/java/
    ├── fake/                              # Test doubles
    ├── integration/                       # Integration tests
    └── unit/                              # Unit tests
```

### Key Components

- **ChatService**: Manages conversation flow, history trimming, and coordinates with the AI client
- **GeminiClient**: Handles HTTP communication with Gemini API, including retry logic and response parsing
- **Message**: Immutable domain object representing a conversation message
- **Role**: Enum defining message roles (USER, MODEL, SYSTEM)

## 🧪 Test-Driven Development

This project was built following TDD principles:

1. **Unit Tests**: Test individual components in isolation using test doubles
2. **Integration Tests**: Test HTTP communication using MockWebServer
3. **Test Coverage**: Enforced through Jacoco Maven plugin (80% line, 70% branch minimum)

### Running Tests

```bash
# Run all tests
mvn test

# Generate coverage report
mvn test jacoco:report

# View coverage report at: target/site/jacoco/index.html
```

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Google Gemini API key

### Installation

1. Clone the repository:
```bash
git clone https://github.com/Patrickbpds/chat-ai.git
cd chat-ai
```

2. Create a `.env` file in the project root:
```bash
cp .env-example .env
```

3. Add your Gemini API key to `.env`:
```
GOOGLE_API_KEY_GEMINI=your_api_key_here
MODEL_ID=gemini-2.5-flash
```

4. Build the project:
```bash
mvn clean install
```

### Running the Application

```bash
mvn exec:java
```

The chat interface will start. Type your messages and press Enter. Type `exit` or `quit` to end the conversation.

Example conversation:
```
Welcome to the Chat Service! Type 'exit' to quit.
You: What is the capital of France?
Gemini: The capital of France is Paris.
You: exit
Goodbye!
```

## 🔧 Configuration

### Environment Variables

- `GOOGLE_API_KEY_GEMINI` (required): Your Gemini API key
- `MODEL_ID` (optional): Gemini model to use (default: `gemini-2.5-flash`)

### ChatService Parameters

```java
ChatService chatService = new ChatService(
    geminiClient,           // AI client implementation
    modelId,                // Model identifier
    systemPrompt,           // System instructions
    maxTurns                // Maximum conversation turns to keep
);
```

- **maxTurns**: Controls how many user-model message pairs to maintain in context (minimum: 2)

## 📊 Test Coverage

The project maintains high test coverage standards:

- **Line Coverage**: ≥80%
- **Branch Coverage**: ≥70%

Coverage is automatically checked during the build process. Builds will fail if coverage thresholds are not met.

## 🔍 Key TDD Lessons

This project demonstrates several TDD best practices:

1. **Test Doubles**: Using fakes and mocks to test in isolation
2. **Integration Testing**: Testing HTTP interactions with MockWebServer
3. **Edge Case Coverage**: Testing error conditions, retries, and boundary cases
4. **Refactoring**: Continuous improvement while maintaining green tests
5. **Design Emergence**: Letting the design evolve from test requirements

## 🛠️ Technologies Used

- **Java 17**: Modern Java features and APIs
- **Maven**: Build automation and dependency management
- **OkHttp**: HTTP client for API communication
- **Jackson**: JSON serialization/deserialization
- **JUnit 5**: Testing framework
- **Mockito**: Mocking framework
- **MockWebServer**: HTTP server mocking for integration tests
- **Jacoco**: Code coverage analysis
- **dotenv-java**: Environment variable management

## 📝 API Integration Details

The project integrates with Google's Gemini API using the following approach:

- **Endpoint**: `/v1beta/models/{model}:generateContent`
- **Retry Strategy**: Exponential backoff for 429 and 5xx errors
- **Max Retries**: 3 attempts with 250ms initial backoff
- **Timeout**: 30 seconds call timeout

## 🤝 Contributing

This is a learning project, but contributions are welcome! Please ensure:

1. All tests pass: `mvn test`
2. Code coverage meets thresholds: `mvn jacoco:check`
3. Follow the existing code style
4. Write tests before implementation (TDD approach)

## 📄 License

This project is open source and available for educational purposes.

---

**Author**:
- **LinkedIn**: [Patrick Batista](https://www.linkedin.com/in/patrick-development/)
- **GitHub**: [@Patrickbpds](https://github.com/Patrickbpds)