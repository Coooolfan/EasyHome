package com.coooolfan.easyhome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coooolfan.easyhome.pojo.entity.HouseUserRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
@Mapper
public interface HouseUserRelationMapper
        extends BaseMapper<HouseUserRelation> {
    @Select("SELECT user_id FROM house_user_relation WHERE house_id = #{houseId}")
    Long getUserIdByHouseId(Long houseId);

    @Select("SELECT house_id FROM house_user_relation WHERE user_id = #{userId}")
    List<Long> getHouseIdsByUserId(@Param("userId") Long userId);


}
