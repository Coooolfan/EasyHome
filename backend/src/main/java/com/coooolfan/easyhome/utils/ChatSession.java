package com.coooolfan.easyhome.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.noear.solon.ai.chat.message.ChatMessage;

import java.util.ArrayList;

@Data
@AllArgsConstructor
public class ChatSession {
    private String uuid;

    private boolean isSteaming = false;

    private ArrayList<ChatMessage> chatContent;
}
