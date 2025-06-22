package com.coooolfan.easyhome.config;

import org.noear.solon.ai.chat.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMConfig {

    @Value("${llm.api-url}")
    private String apiUrl;

    @Value("${llm.api-key}")
    private String apiKey;

    @Value("${llm.model}")
    private String model;

    @Bean
    public ChatModel chatModel() {
        return ChatModel.of(apiUrl)
                .apiKey(apiKey)
                .model(model)
                .build();
    }
}
