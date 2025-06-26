package com.coooolfan.easyhome.message;

import com.coooolfan.easyhome.pojo.dto.HouseDTO;
import com.coooolfan.easyhome.service.HouseRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author lima
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class HouseInfoConsumer {

    private final ObjectMapper objectMapper;
    private final HouseRecordService houseRecordService;

    @KafkaListener(topics = "house-publish-topic", groupId = "admin-group")
    public void consume(String message) {
        try {
            HouseDTO dto = objectMapper.readValue(message, HouseDTO.class);
            log.info("后台收到房源信息: {}", dto);

            // 更新发布记录为已接收
            houseRecordService.markAsReceived(dto);

        } catch (Exception e) {
            log.error("消费房源信息失败", e);
        }
    }
}

