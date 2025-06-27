package com.coooolfan.easyhome.message;

import com.coooolfan.easyhome.pojo.dto.AppointmentDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author lima
 * @version 0.0.1
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "appointment-topic";

    public void send(AppointmentDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            kafkaTemplate.send(TOPIC, json);
            log.info("发送预约Kafka消息: {}", json);
        } catch (Exception e) {
            log.error("发送预约Kafka消息失败", e);
            throw new RuntimeException("发送预约Kafka消息失败", e);
        }
    }
}

