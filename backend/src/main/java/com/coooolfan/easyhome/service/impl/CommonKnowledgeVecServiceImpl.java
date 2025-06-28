package com.coooolfan.easyhome.service.impl;

import com.coooolfan.easyhome.constant.LLMConstant;
import com.coooolfan.easyhome.mapper.CommonKnowledgeVecMapper;
import com.coooolfan.easyhome.pojo.entity.CommonKnowledgeVec;
import com.coooolfan.easyhome.service.CommonKnowledgeVecService;
import com.coooolfan.easyhome.utils.EmbeddingUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.noear.solon.ai.chat.ChatModel;
import org.noear.solon.ai.chat.message.ChatMessage;
import org.noear.solon.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.coooolfan.easyhome.service.impl.HouseServiceImpl.getPromptBuilder;

@Service
@Slf4j
@AllArgsConstructor
public class CommonKnowledgeVecServiceImpl implements CommonKnowledgeVecService {

    private final EmbeddingModel embed;

    private final CommonKnowledgeVecMapper mapper;

    private final ChatModel rewriteModel;

    @Override
    @SneakyThrows
    public List<String> getCommonKnowledgeByVectorSearch(String query, int limit) {
        var embedding = embed.input(query).call().getData().getFirst().getEmbedding();
        embedding = EmbeddingUtils.cut(embedding);
        List<Long> similarHouses = mapper.findSimilarCommonKnowledge(Arrays.toString(embedding), limit);
        return mapper.selectBatchIds(similarHouses).stream()
                .map(CommonKnowledgeVec::getContent).collect(Collectors.toList());
    }

    @Override
    public String getCommonKnowledgeDescByVectorSearch(ChatMessage message, int limit) {
        val regQuestion = message.getContent();
        val commonKnowledgeByVectorSearch = this.getCommonKnowledgeByVectorSearch(regQuestion, limit);
        StringBuilder sb = new StringBuilder();
        for (val com : commonKnowledgeByVectorSearch) {
            sb.append(com).append("\n");
        }
        return String.format("<authoritative-information>\n%s\n</authoritative-information>", sb);
    }

    @Override
    @SneakyThrows
    public String getCommonKnowledgeDescByVectorSearch(ArrayList<ChatMessage> historyMessage, ChatMessage question, int limit) {
        StringBuilder ragPromptBuilder = getPromptBuilder(historyMessage);
        String ragPrompt = String.format(LLMConstant.RAG_REWRITE, ragPromptBuilder, question.getContent());
        val rewriteResp = rewriteModel.prompt(ragPrompt).call();
        val regQuestion = rewriteResp.getAggregationMessage().getContent();
        val commonKnowledgeByVectorSearch = this.getCommonKnowledgeByVectorSearch(regQuestion, limit);
        StringBuilder sb = new StringBuilder();
        for (val com : commonKnowledgeByVectorSearch) {
            sb.append(com).append("\n");
        }
        return String.format("<authoritative-information>\n%s\n</authoritative-information>", sb);
    }
}
