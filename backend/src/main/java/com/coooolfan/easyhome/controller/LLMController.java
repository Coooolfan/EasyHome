package com.coooolfan.easyhome.controller;

import com.coooolfan.easyhome.pojo.dto.LLMStreamDTO;
import com.coooolfan.easyhome.pojo.dto.StreamChatResp;
import com.coooolfan.easyhome.service.LLMService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.ai.chat.ChatModel;
import org.noear.solon.annotation.Produces;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Slf4j
@RequestMapping("/api/chat")
@RestController
@AllArgsConstructor
@Tag(name = "聊天接口组")
public class LLMController {

    private ChatModel chatModel;

    private LLMService llmService;

    @Produces(MediaType.TEXT_PLAIN_VALUE)
    @GetMapping("call")
    @Operation(summary = "调用聊天模型接口，非流")
    public String call(String message) throws Exception {
        UUID uuid = UUID.randomUUID();
        log.info("chat {} prompt: {}", uuid, message);
        String resp = chatModel.prompt(message).call()
                .getMessage()
                .getContent();
        log.info("chat {} response: {}", uuid, resp);
        return resp;
    }

    @PostMapping("stream")
    @Operation(summary = "流式聊天接口")
    public Flux<StreamChatResp> stream(@RequestBody LLMStreamDTO message) {
        return llmService.stream(message);
    }
}






