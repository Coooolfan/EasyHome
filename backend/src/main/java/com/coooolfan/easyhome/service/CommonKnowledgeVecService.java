package com.coooolfan.easyhome.service;

import org.noear.solon.ai.chat.message.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public interface CommonKnowledgeVecService {
    List<String> getCommonKnowledgeByVectorSearch(String query, int limit);

    String getCommonKnowledgeDescByVectorSearch(ChatMessage message, int limit);

    String getCommonKnowledgeDescByVectorSearch(ArrayList<ChatMessage> historyMessage, ChatMessage question, int limit);
}
