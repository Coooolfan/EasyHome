package com.coooolfan.easyhome.pojo.vo;

import lombok.Data;

/**
 * @author lima
 * @version 0.0.1
 **/
@Data
public class HouseQueryVO {
    private String title;
    private String address;
    private Integer minPrice;
    private Integer maxPrice;
    private Integer minArea;
    private Integer maxArea;
    private String rooms;
    private String decoration;
    private String orientation;
    private Integer minYear;
    private Integer maxYear;
    private String sortBy;
}
