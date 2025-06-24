package com.coooolfan.easyhome.pojo;

public class CONSTANT {
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
            """.trim();
}
