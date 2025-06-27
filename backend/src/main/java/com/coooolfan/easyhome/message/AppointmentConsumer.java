package com.coooolfan.easyhome.message;

import cn.dev33.satoken.stp.StpUtil;
import com.coooolfan.easyhome.pojo.dto.AppointmentDTO;
import com.coooolfan.easyhome.service.AppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author lima
 * @version 0.0.1
 **/
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentConsumer {

    private final ObjectMapper objectMapper;
    private final AppointmentService appointmentService;

    @KafkaListener(topics = "appointment-topic", groupId = "appointment-group")
    public void consume(String message) {
        try {

            AppointmentDTO dto = objectMapper.readValue(message, AppointmentDTO.class);
            appointmentService.handle(dto);

        } catch (Exception e) {
            log.error("预约消息消费失败", e);
        }
    }
}