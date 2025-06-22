package com.coooolfan.easyhome.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StreamChatResp {
    String content;
    String role;
    boolean finished;
    String AggregationMessage;
}