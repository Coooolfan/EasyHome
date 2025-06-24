package com.coooolfan.easyhome.config;

import org.noear.solon.ai.chat.ChatModel;
import org.noear.solon.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMConfig {

    @Value("${llm.api-url}")
    private String llmApiUrl;

    @Value("${llm.api-key}")
    private String llmApiKey;

    @Value("${llm.model}")
    private String llmModel;

    @Bean
    public ChatModel chatModel() {
        return ChatModel.of(llmApiUrl)
                .apiKey(llmApiKey)
                .model(llmModel)
                .build();
    }

    @Bean
    public ChatModel rewriteModel() {
        return ChatModel.of(llmApiUrl)
                .apiKey(llmApiKey)
                .model(llmModel)
                .build();
    }

    @Value("${embed.api-url}")
    private String embedApiUrl;

    @Value("${embed.api-key}")
    private String embedApiKey;

    @Value("${embed.model}")
    private String embedModel;

    @Bean
    public EmbeddingModel embedModel() {
        return EmbeddingModel.of(embedApiUrl)
                .apiKey(embedApiKey)
                .model(embedModel)
                .build();
    }
}
