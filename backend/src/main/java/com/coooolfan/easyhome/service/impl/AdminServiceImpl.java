package com.coooolfan.easyhome.service.impl;

import com.coooolfan.easyhome.mapper.HouseMapper;
import com.coooolfan.easyhome.mapper.HouseVecMapper;
import com.coooolfan.easyhome.pojo.entity.House;
import com.coooolfan.easyhome.service.AdminService;
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
            String str = toString(house);
            EmbeddingResponse resp = embeddingModel.input(str).call();
            float[] embedding = resp.getData().getFirst().getEmbedding();

            // 将float[]转换为PostgreSQL vector格式的字符串: [1,2,3,...]
            String vectorStr = Arrays.toString(embedding);

            houseVecMapper.insertHouseVec(house.getId(), vectorStr);
        }
    }

    private String toString(House house) {
        return "房屋位于" + house.getAddress() + "。" +
                "房屋户型为" + house.getRooms() + "，" +
                "面积为" + house.getArea() + "平方米，" +
                "总价为" + house.getPrice() + "元，" +
                "单价为" + house.getUnitPrice() + "元/平方米，" +
                "房屋朝向为" + house.getOrientation() + "，" +
                "装修情况为" + house.getDecoration() + "，" +
                "楼层为" + house.getFloor() + "，" +
                "卖点有" + house.getTitle() + "。";
    }
}
