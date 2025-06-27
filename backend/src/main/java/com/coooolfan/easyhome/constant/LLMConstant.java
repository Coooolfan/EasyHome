package com.coooolfan.easyhome.constant;

import com.coooolfan.easyhome.pojo.dto.StreamChatResp;

public class LLMConstant {
    public static final String RAG_REWRITE = """
            Given the following conversation and a follow-up question, rephrase the follow up question to be a standalone question, in its original language. Keep as much details as possible from previous messages. Keep entity names and all. 
            the chat history:
            %s
            ---
            the follow up question:
            %s
            """.trim();

    public static final String SYSTEM_MESSAGE = """
            You are a helpful assistant. You can answer questions about the house, such as the number of rooms, the area, the price, and so on.
            If you don't know the answer, you can ask the user to provide more information about what they are looking for.
            If the user asks about something unrelated to the house, you can politely refuse to answer.
            <IMPORTANT> Please do not answer questions that are not related to the house, such as personal information, opinions, or other topics. </IMPORTANT>
            <REFERENCE> You can refer to the house information, you have to follow the rule to show the reference like [^${houseID}]. </REFERENCE>
            <REFERENCE-EXAMPLE meaning="引用ID为27的房屋"> [^27] </REFERENCE-EXAMPLE>
            """.trim();

    public static final String SYSTEM_INTERCEPT_MESSAGE = """
            作为一个专业的二手房推荐机器人，现在用户提出了一个问题，同时还会向你提供用户之前沟通的上下文信息，现在你需要判断用户的问题和二手房推荐有没有相关，
            如果用户提的问题和二手房没有任何相关，你应当回复`AGAIN`，表示无法回答用户的问题，请用户提供更多的信息或者重新提问。
            如果用户的回复和相关上下文与房源**相关**，但是如果用户提供的信息量过少，问题过于模糊，你应当回复`MORE`，表示需要更多的信息来回答用户的问题，你应当**注意结合用户的上下文信息**，慎重给出此回复。
            如果用户的问题和上下文信息与房源相关，并结合上下文有一些具体的信息，你应当直接回复`CONTINUE`，表示可以继续回答用户的问题。
            除了这三个单词之外，你不需要回复任何其他内容。
            <CHAT_HISTORY> %s </CHAT_HISTORY>
            <USER_QUEST> %s </USER_QUEST>
            """.trim();

    public static final StreamChatResp REJECT_STREAM_CHAT_RESP = new StreamChatResp(
            "我擅长回答与二手房相关的问题，如果你有其他问题，请重新提问或者提供更多信息。",
            "System",
            true,
            "我擅长回答与二手房相关的问题，如果你有其他问题，请重新提问或者提供更多信息。"
    );
    public static final StreamChatResp MORE_STEAM_CHAT_RESP = new StreamChatResp(
            "请提供更多信息，我需要更多的上下文来回答你的问题，比如房屋的具体位置、户型、面积等信息。",
            "System",
            true,
            "请提供更多信息，我需要更多的上下文来回答你的问题，比如房屋的具体位置、户型、面积等信息。"
    );
}
