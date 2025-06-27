package com.coooolfan.easyhome.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.noear.solon.ai.chat.message.AssistantMessage;
import org.noear.solon.ai.chat.message.ChatMessage;

@Data
@AllArgsConstructor
public class StreamChatResp {
    String content;
    String role;
    boolean finished;
    String aggregationMessage;

    public ChatMessage toChatMessage() {
        return new AssistantMessage(
                this.content
        );
    }
}