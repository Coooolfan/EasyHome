package com.coooolfan.easyhome.pojo.dto;

import lombok.Data;

@Data
public class LLMStreamDTO {
    // 聊天id
    private String uuid;
    // 消息内容
    private String message;
}
