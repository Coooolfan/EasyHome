package com.coooolfan.easyhome.utils;

import com.coooolfan.easyhome.pojo.entity.House;

public class EasyHomeUtils {
    public static String toString(House house){
        return "此房屋位于" + house.getAddress() + "。" +
                "房屋户型为" + house.getRooms() + "，" +
                "面积为" + house.getArea() + "平方米，" +
                "总价为" + house.getPrice() + "元，" +
                "单价为" + house.getUnitPrice() + "元/平方米，" +
                "房屋朝向为" + house.getOrientation() + "，" +
                "装修情况为" + house.getDecoration() + "，" +
                "楼层为" + house.getFloor() + "，" +
                "卖点有" + house.getTitle() + "。";
    }
}
