package com.coooolfan.easyhome.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author lima
 * @version 0.0.1
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("appointment_records")
public class AppointmentRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long houseId;

    private String name;

    private String phone;

    /**
     * 时间段，如 "10:00-11:00"
     */
    private String time;

    private LocalDate date;

    private String remarks;

    /**
     * 状态：PENDING/CONFIRMED/CANCELLED
     */
    private String status;

    private Long userId;


    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

