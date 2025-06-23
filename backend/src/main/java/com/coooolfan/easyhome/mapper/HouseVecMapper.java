package com.coooolfan.easyhome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coooolfan.easyhome.pojo.entity.HouseVec;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

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
    
    /**
     * 根据向量查询最相似的房源ID列表
     * 
     * @param embedding 向量数据
     * @param limit 返回结果数量
     * @return 房源ID列表
     */
    @Select("SELECT house_id FROM houses_vec ORDER BY embedding <-> #{embedding}::vector LIMIT #{limit}")
    List<Long> findSimilarHouses(@Param("embedding") String embedding, @Param("limit") int limit);
} 