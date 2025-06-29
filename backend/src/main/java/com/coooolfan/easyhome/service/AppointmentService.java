package com.coooolfan.easyhome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coooolfan.easyhome.pojo.dto.AppointmentDTO;
import com.coooolfan.easyhome.pojo.entity.AppointmentRecord;
import com.coooolfan.easyhome.pojo.entity.ReviewResult;

import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
public interface AppointmentService extends IService<AppointmentRecord> {
    void handle(AppointmentDTO appointmentDTO);

    ReviewResult review(Long id, Boolean pass, String reason);

    List<AppointmentRecord> getAppointments();
}
