package com.coooolfan.easyhome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coooolfan.easyhome.pojo.entity.HouseVec;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 房屋向量Mapper
 */
@Mapper
public interface HouseVecMapper extends BaseMapper<HouseVec> {
    /**
     * 直接使用JDBC插入向量数据
     * 
     * @param houseId 房屋ID
     * @param embedding 向量数据
     */
    @Insert("INSERT INTO houses_vec(house_id, embedding) VALUES(#{houseId}, #{embedding}::vector)")
    void insertHouseVec(@Param("houseId") Long houseId, @Param("embedding") String embedding);
    
    /**
     * 清空向量表
     */
    @Update("TRUNCATE TABLE houses_vec")
    void clearAll();
} 