package com.coooolfan.easyhome.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 房屋向量实体类，对应houses_vec表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("houses_vec")
public class HouseVec {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("house_id")
    private Long houseId;

    @TableField("embedding")
    private float[] embedding;
} 