package com.coooolfan.easyhome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coooolfan.easyhome.mapper.HouseMapper;
import com.coooolfan.easyhome.mapper.HouseVecMapper;
import com.coooolfan.easyhome.pojo.CONSTANT;
import com.coooolfan.easyhome.pojo.entity.House;
import com.coooolfan.easyhome.pojo.vo.HouseQueryVO;
import com.coooolfan.easyhome.service.HouseService;
import com.coooolfan.easyhome.utils.EasyHomeUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.noear.solon.ai.chat.ChatModel;
import org.noear.solon.ai.chat.ChatRole;
import org.noear.solon.ai.chat.message.ChatMessage;
import org.noear.solon.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
@Service
@AllArgsConstructor
public class HouseServiceImpl extends ServiceImpl<HouseMapper, House> implements HouseService {

    private EmbeddingModel embed;

    private HouseVecMapper houseVecMapper;

    private HouseMapper houseMapper;

    private ChatModel rewriteModel;

    @Override
    public IPage<House> getByPage(Page<House> page, HouseQueryVO queryVO) {
        QueryWrapper<House> queryWrapper = buildQueryWrapper(queryVO);
        return this.page(page, queryWrapper);
    }


    @Override
    @SneakyThrows
    public List<House> getHousesByVectorSearch(String query, int limit) {
        val embedding = embed.input(query).call().getData().getFirst().getEmbedding();
        List<Long> similarHouses = houseVecMapper.findSimilarHouses(Arrays.toString(embedding), limit);
        return houseMapper.selectBatchIds(similarHouses);
    }


    @Override
    public String getHousesDescByVectorSearch(ArrayList<ChatMessage> historyMessage, ChatMessage question, int limit) {
        StringBuilder ragPromptBuilder = new StringBuilder();
        for (ChatMessage chatMessage : historyMessage) {
            if (ChatRole.ASSISTANT == chatMessage.getRole())
                ragPromptBuilder.append("ASSISTANT: \n").append(chatMessage.getContent());

            if (ChatRole.USER == chatMessage.getRole())
                ragPromptBuilder.append("USER: \n").append(chatMessage.getContent()).append("\n");

        }
        String ragPrompt = String.format(CONSTANT.RAG_REWRITE, ragPromptBuilder, question.getContent());
        try {
            val rewriteResp = rewriteModel.prompt(ragPrompt).call();
            val regQuestion = rewriteResp.getAggregationMessage().getContent();
            val housesByVectorSearch = this.getHousesByVectorSearch(regQuestion, limit);
            StringBuilder sb = new StringBuilder();
            for (val house : housesByVectorSearch) {
                sb.append(EasyHomeUtils.toString(house)).append("\n");
            }
            return String.format("<authoritative-information>\n%s\n</authoritative-information>",sb);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getHousesDescByVectorSearch(ChatMessage chatMessage, int limit) {
        val regQuestion = chatMessage.getContent();
        val housesByVectorSearch = this.getHousesByVectorSearch(regQuestion, limit);
        StringBuilder sb = new StringBuilder();
        for (val house : housesByVectorSearch) {
            sb.append(EasyHomeUtils.toString(house)).append("\n");
        }
        return String.format("<authoritative-information>\n%s\n</authoritative-information>",sb);
    }


    private QueryWrapper<House> buildQueryWrapper(HouseQueryVO queryVO) {
        QueryWrapper<House> queryWrapper = new QueryWrapper<>();

        if (queryVO != null) {
            // 标题模糊查询
            if (StringUtils.hasText(queryVO.getTitle())) {
                queryWrapper.like("title", queryVO.getTitle());
            }

            // 地址模糊查询
            if (StringUtils.hasText(queryVO.getAddress())) {
                queryWrapper.like("address", queryVO.getAddress());
            }

            // 价格范围查询
            if (queryVO.getMinPrice() != null) {
                queryWrapper.ge("price", queryVO.getMinPrice());
            }
            if (queryVO.getMaxPrice() != null) {
                queryWrapper.le("price", queryVO.getMaxPrice());
            }

            // 面积范围查询
            if (queryVO.getMinArea() != null) {
                queryWrapper.ge("area", queryVO.getMinArea());
            }
            if (queryVO.getMaxArea() != null) {
                queryWrapper.le("area", queryVO.getMaxArea());
            }

            // 户型查询
            if (StringUtils.hasText(queryVO.getRooms())) {
                queryWrapper.like("rooms", queryVO.getRooms());
            }

            // 装修状况查询
            if (StringUtils.hasText(queryVO.getDecoration())) {
                queryWrapper.eq("decoration", queryVO.getDecoration());
            }

            // 朝向查询
            if (StringUtils.hasText(queryVO.getOrientation())) {
                queryWrapper.eq("orientation", queryVO.getOrientation());
            }

            // 建造年份范围查询
            if (queryVO.getMinYear() != null) {
                queryWrapper.ge("build_year", queryVO.getMinYear());
            }
            if (queryVO.getMaxYear() != null) {
                queryWrapper.le("build_year", queryVO.getMaxYear());
            }

            // 排序
            if (StringUtils.hasText(queryVO.getSortBy())) {
                switch (queryVO.getSortBy()) {
                    case "price-asc":
                        queryWrapper.orderByAsc("price");
                        break;
                    case "price-desc":
                        queryWrapper.orderByDesc("price");
                        break;
                    case "area-asc":
                        queryWrapper.orderByAsc("area");
                        break;
                    case "area-desc":
                        queryWrapper.orderByDesc("area");
                        break;
                    case "time-desc":
                    default:
                        queryWrapper.orderByDesc("created_at");
                        break;
                }
            } else {
                queryWrapper.orderByDesc("created_at");
            }
        }

        return queryWrapper;
    }
}
