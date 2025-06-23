package com.coooolfan.easyhome.config;

import org.noear.solon.ai.chat.ChatModel;
import org.noear.solon.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMConfig {

    @Value("${llm.api-url}")
    private String LlmApiUrl;

    @Value("${llm.api-key}")
    private String LlmApiKey;

    @Value("${llm.model}")
    private String LlmModel;

    @Bean
    public ChatModel chatModel() {
        return ChatModel.of(LlmApiUrl)
                .apiKey(LlmApiKey)
                .model(LlmModel)
                .build();
    }

    @Value("${embed.api-url}")
    private String EmbedApiUrl;

    @Value("${embed.api-key}")
    private String EmbedApiKey;

    @Value("${embed.model}")
    private String EmbedModel;

    @Bean
    public EmbeddingModel embedModel() {
        return EmbeddingModel.of(EmbedApiUrl)
                .apiKey(EmbedApiKey)
                .model(EmbedModel)
                .build();
    }
}
