package com.coooolfan.easyhome.service.impl;

import com.coooolfan.easyhome.constant.LLMConstant;
import com.coooolfan.easyhome.pojo.dto.LLMStreamDTO;
import com.coooolfan.easyhome.pojo.vo.StreamChatResp;
import com.coooolfan.easyhome.service.HouseService;
import com.coooolfan.easyhome.service.LLMService;
import com.coooolfan.easyhome.utils.ChatSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.ai.chat.ChatModel;
import org.noear.solon.ai.chat.message.ChatMessage;
import org.noear.solon.ai.chat.message.SystemMessage;
import org.noear.solon.ai.chat.message.UserMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
//@Service
@RequestMapping("/api/chat")
@RestController
@AllArgsConstructor
@Tag(name = "聊天接口组")
public class LLMServiceImpl implements LLMService {

    private final static ConcurrentHashMap<String, ChatSession> chatMap = new ConcurrentHashMap<>();

    private HouseService houseService;

    private ChatModel chatModel;

    @Override
    @PostMapping("stream")
    @Operation(summary = "流式聊天接口")
    public Flux<StreamChatResp> stream(@RequestBody LLMStreamDTO message) {
        ArrayList<ChatMessage> chatHistory;
        ChatSession chatSession;
        if (chatMap.containsKey(message.getUuid())) {
            chatSession = chatMap.get(message.getUuid());
            if (chatSession.isSteaming()) {
                log.warn("当前会话正在进行中，请稍后再试");
                return Flux.just(new StreamChatResp(
                        "当前会话正在进行中，请稍后再试",
                        "System",
                        true,
                        "当前会话正在进行中，请稍后再试"
                ));
            }
            chatSession.setSteaming(true);
            // 浅拷贝聊天记录
            chatHistory = new ArrayList<>(chatSession.getChatContent());
            // 向聊天记录中添加用户消息 RAG消息不需要添加
            chatSession.getChatContent().add(new UserMessage(message.getMessage()));
            // 拼接RAG 与 用户的新消息 发送至LLM
            chatHistory.add(new UserMessage(houseService.getHousesDescByVectorSearch(chatHistory, new UserMessage(message.getMessage()), 5) + "\n" + message.getMessage()));

        } else {
            chatSession = new ChatSession(message.getUuid(), false, new ArrayList<>());
            chatSession.getChatContent().add(new SystemMessage(LLMConstant.SYSTEM_MESSAGE));
            chatSession.setSteaming(true);
            // 浅拷贝聊天记录
            chatHistory = new ArrayList<>(chatSession.getChatContent());
            // 向聊天记录中添加用户消息 RAG消息不需要添加
            chatSession.getChatContent().add(new UserMessage(message.getMessage()));
            chatMap.put(message.getUuid(), chatSession);
            // 拼接RAG 与 用户的新消息 发送至LLM
            chatHistory.add(new UserMessage(houseService.getHousesDescByVectorSearch(new UserMessage(message.getMessage()), 5) + "\n" + message.getMessage()));
        }
        log.info("Start stream chat {} connection", message.getUuid());
        return Flux.from(chatModel.prompt(chatHistory).stream())
                .map(resp -> {
                    if (resp.isFinished())
                        chatSession.getChatContent().add(resp.getAggregationMessage());
                    log.info("{}:isfinished:{},content: {}", message.getUuid(), resp.isFinished(), resp.getMessage().getContent());
                    return new StreamChatResp(
                            resp.getMessage().getContent(),
                            "Assistant",
                            resp.isFinished(),
                            resp.getAggregationMessage().getContent()
                    );
                })
                .doOnComplete(() -> {
                    chatSession.setSteaming(false);
                    log.info("stream chat {} connection closed", message.getUuid());
                });

    }
}