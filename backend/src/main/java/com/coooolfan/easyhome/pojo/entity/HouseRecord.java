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
 * 房源发布记录实体类
 *
 * @author lima
 * @version 0.0.1
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName(value = "house_records")
public class HouseRecord {
    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发布用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 房源标题
     */
    private String title;

    /**
     * 房源地址
     */
    private String address;

    /**
     * 房源总价(元)
     */
    private Integer price;

    /**
     * 房源面积(平方米)
     */
    private Integer area;

    /**
     * 房源单价(元/平方米)
     */
    @TableField("unit_price")
    private Integer unitPrice;

    /**
     * 户型
     */
    private String rooms;

    /**
     * 楼层
     */
    private String floor;

    /**
     * 建筑年代
     */
    @TableField("build_year")
    private Integer buildYear;

    /**
     * 朝向
     */
    private String orientation;

    /**
     * 装修情况
     */
    private String decoration;

    /**
     * 房源标签(JSONB存储的数组)
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> tag;

    /**
     * 房源图片URL
     */
    private String image;

    /**
     * 审核状态: PENDING(待审核), APPROVED(已通过), REJECTED(已拒绝)
     */
    private String status;

    /**
     * 拒绝原因(如果有)
     */
    private String reason;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}