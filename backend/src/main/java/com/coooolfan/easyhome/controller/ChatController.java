//package com.coooolfan.easyhome.controller;
//
//import com.coooolfan.easyhome.pojo.vo.StreamChatResp;
//import jakarta.annotation.Resource;
//import org.springframework.ai.chat.messages.AssistantMessage;
//import org.springframework.ai.chat.messages.Message;
//import org.springframework.ai.chat.messages.UserMessage;
//import org.springframework.ai.chat.prompt.Prompt;
//import org.springframework.ai.deepseek.DeepSeekChatModel;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Flux;
//
//import java.util.ArrayList;
//import java.util.List;
//
//
///**
// * @author lima
// * @version 0.0.1
// **/
//@RestController
//public class ChatController {
//
//    @Resource
//    private DeepSeekChatModel deepSeekChatModel;
//
//    @GetMapping("/api/chat/stream")
//    public Flux<StreamChatResp> stream(@RequestParam(value = "message") String message) {
//        Prompt prompt = new Prompt(new UserMessage(message));
//        return deepSeekChatModel.stream(prompt)
//                .scan(new Object[]{new StringBuilder(), false}, (acc, chatResponse) -> {
//                    String newContent = chatResponse.getResults().getFirst().getOutput().getText();
//
//                    StringBuilder aggregated = (StringBuilder) acc[0];
//                    boolean shouldFinish = false;
//
//                    if (newContent != null && !newContent.isEmpty()) {
//                        aggregated.append(newContent);
//                    }
//                    return new Object[]{aggregated, shouldFinish};
//                })
//                .skip(1)
//                .map(acc -> {
//                    StringBuilder aggregated = (StringBuilder) acc[0];
//                    boolean finished = (boolean) acc[1];
//                    return new StreamChatResp(
//                            aggregated.toString(),
//                            "Assistant",
//                            finished,
//                            aggregated.toString()
//                    );
//                });
//    }
//
//
//    private final List<Message> chatHistory = new ArrayList<>();
//
//    /**
//     * 使用 DeepSeek 模型进行流式聊天
//     *
//     * @param message 用户输入的消息
//     * @return 返回一个 Flux 流，包含实时生成的聊天响应
//     */
//    @GetMapping("/api/chat/streamWithDeepSeek")
//    // TODO 记忆聊天
//    public Flux<StreamChatResp> generateStream(@RequestParam("message") String message) {
//        // 1. 将用户消息加入上下文
//        chatHistory.add(new UserMessage(message));
//
//        // 2. 构造 Prompt 包含完整上下文
//        Prompt prompt = new Prompt(chatHistory);
//
//        return deepSeekChatModel.stream(prompt)
//                .scan(new Object[]{new StringBuilder(), false}, (acc, chatResponse) -> {
//                    String newContent = chatResponse.getResults().getFirst().getOutput().getText();
//
//                    StringBuilder aggregated = (StringBuilder) acc[0];
//                    boolean shouldFinish = false;
//
//                    if (newContent != null && !newContent.isEmpty()) {
//                        aggregated.append(newContent);
//                    }
//                    return new Object[]{aggregated, shouldFinish};
//                })
//                .skip(1)
//                .map(acc -> {
//                    StringBuilder aggregated = (StringBuilder) acc[0];
//                    boolean finished = (boolean) acc[1];
//
//                    if (finished) {
//                        chatHistory.add(new AssistantMessage(aggregated.toString()));
//                    }
//
//                    return new StreamChatResp(
//                            aggregated.toString(),
//                            "Assistant",
//                            finished,
//                            aggregated.toString()
//                    );
//                });
//    }
//
//
//}
