package com.coooolfan.easyhome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coooolfan.easyhome.pojo.entity.House;
import com.coooolfan.easyhome.pojo.vo.HouseQueryVO;
import org.noear.solon.ai.chat.message.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
public interface HouseService extends IService<House> {
    IPage<House> getByPage(Page<House> page, HouseQueryVO queryVO);

    List<House> getHousesByVectorSearch(String query, int limit);

    String getHousesDescByVectorSearch(ArrayList<ChatMessage> historyMessage, ChatMessage question, int limit);

    String getHousesDescByVectorSearch(ChatMessage chatMessage, int limit);
}