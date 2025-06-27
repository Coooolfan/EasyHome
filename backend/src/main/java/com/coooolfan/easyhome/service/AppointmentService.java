package com.coooolfan.easyhome.service;

import com.coooolfan.easyhome.pojo.dto.AppointmentDTO;
import com.coooolfan.easyhome.pojo.entity.ReviewResult;

/**
 * @author lima
 * @version 0.0.1
 **/
public interface AppointmentService {
    void handle(AppointmentDTO appointmentDTO);

    ReviewResult review(Long id, Boolean pass, String reason);
}
