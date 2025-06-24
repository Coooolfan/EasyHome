package com.coooolfan.easyhome.service;

import com.coooolfan.easyhome.pojo.dto.LLMStreamDTO;
import com.coooolfan.easyhome.pojo.vo.StreamChatResp;
import reactor.core.publisher.Flux;

public interface LLMService {
    Flux<StreamChatResp> stream(LLMStreamDTO message);
}
