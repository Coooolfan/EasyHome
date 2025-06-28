package com.coooolfan.easyhome.service.impl;

import com.coooolfan.easyhome.constant.LLMConstant;
import com.coooolfan.easyhome.pojo.dto.LLMStreamDTO;
import com.coooolfan.easyhome.pojo.dto.StreamChatResp;
import com.coooolfan.easyhome.service.CommonKnowledgeVecService;
import com.coooolfan.easyhome.service.HouseService;
import com.coooolfan.easyhome.service.LLMService;
import com.coooolfan.easyhome.utils.ChatSession;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.noear.solon.ai.chat.ChatModel;
import org.noear.solon.ai.chat.message.ChatMessage;
import org.noear.solon.ai.chat.message.SystemMessage;
import org.noear.solon.ai.chat.message.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static com.coooolfan.easyhome.constant.LLMConstant.MORE_STEAM_CHAT_RESP;
import static com.coooolfan.easyhome.constant.LLMConstant.REJECT_STREAM_CHAT_RESP;

@Slf4j
@Service
@AllArgsConstructor
public class LLMServiceImpl implements LLMService {

    private final static ConcurrentHashMap<String, ChatSession> chatMap = new ConcurrentHashMap<>();
    private final CommonKnowledgeVecService commonKnowledgeVecService;

    private HouseService houseService;

    private ChatModel chatModel;

    @Override
    @SneakyThrows
    public Flux<StreamChatResp> stream(@RequestBody LLMStreamDTO message) {
        ArrayList<ChatMessage> chatHistory;
        ChatSession chatSession;
        if (chatMap.containsKey(message.getUuid())) {
            chatSession = chatMap.get(message.getUuid());
            chatSession.setSteaming(true);
            // 浅拷贝聊天记录
            chatHistory = new ArrayList<>(chatSession.getChatContent());
            // 向聊天记录中添加用户消息 RAG消息不需要添加
            chatSession.getChatContent().add(new UserMessage(message.getMessage()));
            val interceptResp = chatModel.prompt(
                    String.format(
                            LLMConstant.SYSTEM_INTERCEPT_MESSAGE,
                            HouseServiceImpl.getPromptBuilder(chatMap.get(message.getUuid()).getChatContent()),
                            message.getMessage()
                    )).call().getMessage().getContent();
            if (interceptResp.contains("AGAIN")) {
                chatSession.getChatContent().add(REJECT_STREAM_CHAT_RESP.toChatMessage());
                return Flux.just(REJECT_STREAM_CHAT_RESP);
            } else if (interceptResp.contains("MORE")) {
                chatSession.getChatContent().add(MORE_STEAM_CHAT_RESP.toChatMessage());
                return Flux.just(MORE_STEAM_CHAT_RESP);
            }
            // 拼接RAG 与 用户的新消息 发送至LLM
            chatHistory.add(new UserMessage(
                    houseService.getHousesDescByVectorSearch(chatHistory, new UserMessage(message.getMessage()), 5) + "\n"
                            + commonKnowledgeVecService.getCommonKnowledgeDescByVectorSearch(chatHistory, new UserMessage(message.getMessage()), 5) + "\n"
                            + message.getMessage()));

        } else {

            chatSession = new ChatSession(message.getUuid(), false, new ArrayList<>());
            chatSession.getChatContent().add(new SystemMessage(LLMConstant.SYSTEM_MESSAGE));
            chatSession.setSteaming(true);
            // 浅拷贝聊天记录
            chatHistory = new ArrayList<>(chatSession.getChatContent());
            // 向聊天记录中添加用户消息 RAG消息不需要添加
            chatSession.getChatContent().add(new UserMessage(message.getMessage()));
            chatMap.put(message.getUuid(), chatSession);
            val interceptResp = chatModel.prompt(String.format(LLMConstant.SYSTEM_INTERCEPT_MESSAGE, "NONE", message.getMessage())).call().getMessage().getContent();
            if (interceptResp.contains("AGAIN")) {
                chatMap.get(message.getUuid()).getChatContent().add(REJECT_STREAM_CHAT_RESP.toChatMessage());
                return Flux.just(REJECT_STREAM_CHAT_RESP);
            } else if (interceptResp.contains("MORE")) {
                chatMap.get(message.getUuid()).getChatContent().add(MORE_STEAM_CHAT_RESP.toChatMessage());
                return Flux.just(MORE_STEAM_CHAT_RESP);
            }
            // 拼接RAG 与 用户的新消息 发送至LLM
            chatHistory.add(new UserMessage(
                    houseService.getHousesDescByVectorSearch(new UserMessage(message.getMessage()), 5) + "\n"
                            + commonKnowledgeVecService.getCommonKnowledgeDescByVectorSearch(new UserMessage(message.getMessage()), 5) + "\n"
                            + message.getMessage()));
        }
        log.info("Start stream chat {} connection", message.getUuid());
        return Flux.from(chatModel.prompt(chatHistory).stream())
                .map(resp -> {
                    if (resp.isFinished()) {
                        chatSession.getChatContent().add(resp.getAggregationMessage());
                    }
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