package com.coooolfan.easyhome.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.coooolfan.easyhome.handle.StringListTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("houses")
public class House {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String address;
    private Integer price;

    @TableField("unit_price")
    private Integer unitPrice;

    private Integer area;
    private String rooms;
    private String floor;

    @TableField("build_year")
    private Integer buildYear;

    private String orientation;
    private String decoration;
    private String image;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> tag;

    private Long userId;
}

