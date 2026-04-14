package com.example.ai.service;

import com.example.ai.model.response.AIChatResponse;
import com.example.ai.model.response.ActorsFilms;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.AssistantPromptTemplate;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private static final String SENTIMENT_ANALYSIS_TEMPLATE =
            "{query}, You must answer strictly in the following format: one of [POSITIVE, NEGATIVE, SARCASTIC]";

    @Value("classpath:/data/restaurants.json")
    private Resource restaurantsResource;

    @Value("classpath:/rag-prompt-template.st")
    private Resource ragPromptTemplate;

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;

    public ChatService(ChatClient.Builder chatClientBuilder, EmbeddingModel embeddingModel) {
        this.chatClient = chatClientBuilder.build();
        this.embeddingModel = embeddingModel;
    }

    public AIChatResponse chat(String query) {
        try {
            String answer = chatClient.prompt(query).call().content();
            return new AIChatResponse(answer);
        } catch (Exception e) {
            logger.error("Error in chat: {}", e.getMessage(), e);
            return new AIChatResponse("Error: " + e.getMessage());
        }
    }

    public AIChatResponse chatWithPrompt(String query) {
        try {
            PromptTemplate promptTemplate = new PromptTemplate("Tell me a joke about {subject}");
            Prompt prompt = promptTemplate.create(Map.of("subject", query));
            String answer = chatClient.prompt(prompt).call().content();
            return new AIChatResponse(answer);
        } catch (Exception e) {
            logger.error("Error in chatWithPrompt: {}", e.getMessage(), e);
            return new AIChatResponse("Error: " + e.getMessage());
        }
    }

    public AIChatResponse chatWithSystemPrompt(String query) {
        try {
            SystemMessage systemMessage = new SystemMessage("You are a sarcastic and funny chatbot");
            UserMessage userMessage = new UserMessage("Tell me a joke about " + query);
            Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
            String answer = chatClient.prompt(prompt).call().content();
            return new AIChatResponse(answer);
        } catch (Exception e) {
            logger.error("Error in chatWithSystemPrompt: {}", e.getMessage(), e);
            return new AIChatResponse("Error: " + e.getMessage());
        }
    }

    public AIChatResponse analyzeSentiment(String query) {
        try {
            AssistantPromptTemplate promptTemplate = new AssistantPromptTemplate(SENTIMENT_ANALYSIS_TEMPLATE);
            Prompt prompt = promptTemplate.create(Map.of("query", query));
            String answer = chatClient.prompt(prompt).call().content();
            return new AIChatResponse(answer);
        } catch (Exception e) {
            logger.error("Error in analyzeSentiment: {}", e.getMessage(), e);
            return new AIChatResponse("Error: " + e.getMessage());
        }
    }

    public AIChatResponse getEmbeddings(String query) {
        try {
            float[] embedded = embeddingModel.embed(query);
            return new AIChatResponse(Arrays.toString(embedded));
        } catch (Exception e) {
            logger.error("Error in getEmbeddings: {}", e.getMessage(), e);
            return new AIChatResponse("Error: " + e.getMessage());
        }
    }

    public ActorsFilms generateAsBean(String actor) {
        try {
            BeanOutputConverter<ActorsFilms> outputParser = new BeanOutputConverter<>(ActorsFilms.class);

            String format = outputParser.getFormat();
            String template =
                    """
				Generate the filmography for the Indian actor {actor} as of today.
				{format}
				""";

            PromptTemplate promptTemplate = PromptTemplate.builder()
                    .template(template)
                    .variables(Map.of("actor", actor, "format", format))
                    .build();
            Prompt prompt = new Prompt(promptTemplate.createMessage());
            String response = chatClient.prompt(prompt).call().content();

            return outputParser.convert(response);
        } catch (Exception e) {
            logger.error("Error in generateAsBean: {}", e.getMessage(), e);
            return new ActorsFilms("Error", List.of("Error: " + e.getMessage()));
        }
    }

    public AIChatResponse ragGenerate(String query) {
        try {
            // Step 1 - Load JSON document as Documents and save
            logger.info("Loading JSON as Documents and save");
            SimpleVectorStore simpleVectorStore =
                    SimpleVectorStore.builder(embeddingModel).build();

            List<Document> documents = List.of();
            if (restaurantsResource.exists()) { // load existing vector store if exists

                JsonReader documentReader = new JsonReader(
                        restaurantsResource, "address", "borough", "cuisine", "grades", "name", "restaurant_id");
                documents = documentReader.get();
                simpleVectorStore.accept(documents);
            }

            // Step 2 retrieve related documents to query
            logger.info("Retrieving relevant documents");
            List<Document> similarDocuments = simpleVectorStore.similaritySearch(
                    SearchRequest.builder().query(query).topK(2).build());

            // 检查是否找到相关文档
            if (similarDocuments.isEmpty()) {
                return new AIChatResponse("No relevant documents found.");
            }

            logger.info("Found {} relevant documents.", similarDocuments.size());

            List<String> contentList =
                    similarDocuments.stream().map(Document::getText).toList();
            PromptTemplate promptTemplate = new PromptTemplate(ragPromptTemplate);
            Map<String, Object> promptParameters = new HashMap<>();

            promptParameters.put("input", query);
            promptParameters.put("documents", String.join("\n", contentList));
            Prompt prompt = promptTemplate.create(promptParameters);

            String response = chatClient.prompt(prompt).call().content();
            simpleVectorStore.delete(documents.stream().map(Document::getId).toList());
            return new AIChatResponse(response);
        } catch (Exception e) {
            logger.error("Error in ragGenerate: {}", e.getMessage(), e);
            return new AIChatResponse("Error: " + e.getMessage());
        }
    }

    public Flux<String> streamChat(String query) {
        try {
            return chatClient.prompt(query).stream().content();
        } catch (Exception e) {
            logger.error("Error in streamChat: {}", e.getMessage(), e);
            return Flux.just("Error: " + e.getMessage());
        }
    }
}
