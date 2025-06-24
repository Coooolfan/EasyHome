package com.coooolfan.easyhome.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
@Data
public class HouseDTO {
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "地址不能为空")
    private String address;
    @NotBlank(message = "价格不能为空")
    private Integer price;
    @NotBlank(message = "单价不能为空")
    private Integer unitPrice;
    @NotBlank(message = "面积不能为空")
    private Integer area;
    @NotBlank(message = "房间数不能为空")
    private String rooms;
    @NotBlank(message = "楼层不能为空")
    private String floor;
    @NotBlank(message = "建造年份不能为空")
    private Integer buildYear;
    @NotBlank(message = "朝向不能为空")
    private String orientation;
    @NotBlank(message = "装修情况不能为空")
    private String decoration;
    @NotBlank(message = "图片不能为空")
    private String image;
    @NotBlank(message = "标签不能为空")
    private List<String> tag;
}
