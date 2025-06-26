package com.coooolfan.easyhome.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StreamChatResp {
    String content;
    String role;
    boolean finished;
    String aggregationMessage;
}