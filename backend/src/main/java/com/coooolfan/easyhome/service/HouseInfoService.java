package com.coooolfan.easyhome.service;

import com.coooolfan.easyhome.pojo.dto.HouseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * @author lima
 */
public interface HouseInfoService {
    void publish(HouseDTO dto);
}
