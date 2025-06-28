package com.coooolfan.easyhome.service.impl;

import com.coooolfan.easyhome.mapper.CommonKnowledgeVecMapper;
import com.coooolfan.easyhome.mapper.HouseMapper;
import com.coooolfan.easyhome.mapper.HouseVecMapper;
import com.coooolfan.easyhome.pojo.entity.House;
import com.coooolfan.easyhome.service.AdminService;
import com.coooolfan.easyhome.utils.EasyHomeUtils;
import com.coooolfan.easyhome.utils.EmbeddingUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.noear.solon.ai.embedding.EmbeddingModel;
import org.noear.solon.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
@Service
@AllArgsConstructor
public class AdminServiceImpl implements AdminService {

    private HouseMapper houseMapper;

    private HouseVecMapper houseVecMapper;

    private CommonKnowledgeVecMapper commonKnowledgeVecMapper;

    private EmbeddingModel embeddingModel;

    @Override
    @SneakyThrows
    public void resetHouseVec() {
        ArrayList<String> houseStr = new ArrayList<>();
        val houses = houseMapper.selectList(null);

        // 先清空所有向量数据
        houseVecMapper.clearAll();

        for (int i = 0; i < houses.size(); i++) {
            House house = houses.get(i);
            String str = EasyHomeUtils.toString(house);
            EmbeddingResponse resp = embeddingModel.input(str).call();
            float[] embedding = resp.getData().getFirst().getEmbedding();
            embedding = EmbeddingUtils.cut(embedding);
            // 将float[]转换为PostgreSQL vector格式的字符串: [1,2,3,...]
            String vectorStr = Arrays.toString(embedding);

            houseVecMapper.insertHouseVec(house.getId(), vectorStr);

            log.info("已处理第 {} 条房源数据: {}, 进度: {}/{}",
                    i + 1, house.getId(), i + 1, houses.size());
        }
    }

    @Override
    @SneakyThrows
    public void resetCommonKnowledgeVec() {
        val commonKnowledgeVecs = commonKnowledgeVecMapper.selectList(null);
        // 先清空所有向量数据
        commonKnowledgeVecMapper.clearEmbeddings();

        for (int i = 0; i < commonKnowledgeVecs.size(); i++) {
            val commonKnowledgeVec = commonKnowledgeVecs.get(i);
            EmbeddingResponse embeddingResponse = embeddingModel.input(commonKnowledgeVec.getContent()).call();
            float[] embedding = embeddingResponse.getData().getFirst().getEmbedding();
            embedding = EmbeddingUtils.cut(embedding);
            // 将float[]转换为PostgreSQL vector格式的字符串: [1,2,3,...]
            String vectorStr = Arrays.toString(embedding);

            commonKnowledgeVecMapper.updateHouseVec(commonKnowledgeVec.getId(), vectorStr);

            log.info("已处理第 {} 条公共知识数据: {}, 进度: {}/{}",
                    i + 1, commonKnowledgeVec.getId(), i + 1, commonKnowledgeVecs.size());
        }
    }
}
