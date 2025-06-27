package com.coooolfan.easyhome.service.impl;

import com.coooolfan.easyhome.message.HouseInfoProducer;
import com.coooolfan.easyhome.pojo.dto.HouseDTO;
import com.coooolfan.easyhome.service.HouseInfoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * @author lima
 * @version 0.0.1
 **/
@Service
@RequiredArgsConstructor
public class HouseInfoServiceImpl implements HouseInfoService {

    private final HouseInfoProducer houseInfoProducer;

    @Override
    public void publish(HouseDTO dto) {
        houseInfoProducer.send(dto);
    }
}
