package com.coooolfan.easyhome.pojo.dto;

import lombok.Data;

/**
 * @author lima
 * @version 0.0.1
 **/
@Data
public class AppointmentDTO {
    private Long houseId;
    private String name;
    private String phone;
    private String date;
    private String time;
    private String remarks;
    private Long userId;
}
