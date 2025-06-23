package com.coooolfan.easyhome.controller;

import com.coooolfan.easyhome.pojo.vo.StreamChatResp;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.ai.chat.ChatModel;
import org.noear.solon.annotation.Produces;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Slf4j
@RequestMapping("/api/chat")
@RestController
@AllArgsConstructor
public class LLMController {

    ChatModel chatModel;

    @Produces(MediaType.TEXT_PLAIN_VALUE)
    @GetMapping("call")
    public String call(String prompt) throws Exception {
        UUID uuid = UUID.randomUUID();
        log.info("chat {} prompt: {}", uuid, prompt);
        String resp = chatModel.prompt(prompt).call()
                .getMessage()
                .getContent();
        log.info("chat {} response: {}", uuid, resp);
        return resp;
    }

    //    @Produces(MimeType.APPLICATION_X_NDJSON_UTF8_VALUE)
    @GetMapping("stream")
    public Flux<StreamChatResp> stream(String prompt) {
        UUID uuid = UUID.randomUUID();
        log.info("stream chat {} prompt: {}", uuid, prompt);

        return Flux.from(chatModel.prompt(prompt).stream())
                .scan(new Object[]{null, false}, (acc, resp) -> {

                    if (resp.isFinished()) {
                        log.info("LLM返回结束标志，结束流处理");
                    }

                    String currentAggContent = resp.getAggregationMessage().getContent();
                    String previousAggContent = acc[0] != null ? (String) acc[0] : "";
                    boolean shouldFinish = currentAggContent.equals(previousAggContent) && !currentAggContent.isEmpty();
                    return new Object[]{currentAggContent, shouldFinish};
                })
                .skip(1) // 跳过初始状态
                .takeUntil(acc -> (boolean) acc[1]) // 当shouldFinish为true时终止流
                .map(acc -> {
                    String content = (String) acc[0];
                    boolean finished = (boolean) acc[1];

                    return new StreamChatResp(
                            content,
                            "Assistant",
                            finished,
                            content
                    );
                })
                .doOnComplete(() -> log.info("stream chat {} connection closed", uuid));
    }

//    @GetMapping("stream")
//    public Flux<StreamChatResp> stream(String prompt) {
//        UUID uuid = UUID.randomUUID();
//        log.info("stream chat {} prompt: {}", uuid, prompt);
//        return Flux.from(chatModel.prompt(prompt).stream())
//                .map(resp -> {
//                    return new StreamChatResp(
//                            resp.getMessage().getContent(),
//                            "Assistant",
//                            resp.isFinished(),
//                            resp.getAggregationMessage().getContent()
//                    );
//                })
//                .doOnComplete(() -> log.info("stream chat {} connection closed", uuid));
//    }
}






