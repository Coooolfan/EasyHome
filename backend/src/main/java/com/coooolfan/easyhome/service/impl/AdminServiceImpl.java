package com.coooolfan.easyhome.service.impl;

import com.coooolfan.easyhome.mapper.HouseMapper;
import com.coooolfan.easyhome.mapper.HouseVecMapper;
import com.coooolfan.easyhome.pojo.entity.House;
import com.coooolfan.easyhome.service.AdminService;
import com.coooolfan.easyhome.utils.EasyHomeUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.noear.solon.ai.embedding.EmbeddingModel;
import org.noear.solon.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;

@Service
@AllArgsConstructor
public class AdminServiceImpl implements AdminService {

    private HouseMapper houseMapper;

    private HouseVecMapper houseVecMapper;

    private EmbeddingModel embeddingModel;

    @Override
    @SneakyThrows
    public void resetHouseVec() {
        ArrayList<String> houseStr = new ArrayList<>();
        val houses = houseMapper.selectList(null);

        // 先清空所有向量数据
        houseVecMapper.clearAll();

        for (House house : houses) {
            String str = EasyHomeUtils.toString(house);
            EmbeddingResponse resp = embeddingModel.input(str).call();
            float[] embedding = resp.getData().getFirst().getEmbedding();

            // 将float[]转换为PostgreSQL vector格式的字符串: [1,2,3,...]
            String vectorStr = Arrays.toString(embedding);

            houseVecMapper.insertHouseVec(house.getId(), vectorStr);
        }
    }
}
