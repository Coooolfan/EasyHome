package com.coooolfan.easyhome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coooolfan.easyhome.pojo.entity.CommonKnowledgeVec;
import com.coooolfan.easyhome.pojo.entity.HouseVec;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 公共知识向量数据访问层
 */
@Mapper
public interface CommonKnowledgeVecMapper extends BaseMapper<CommonKnowledgeVec> {

    /**
     * 清空向量表
     */
    @Update("UPDATE common_knowledge_vec SET embedding = NULL")
    void clearEmbeddings();
    
    /**
     * 根据向量查询最相似的房源ID列表
     * 
     * @param embedding 向量数据
     * @param limit 返回结果数量
     * @return 房源ID列表
     */
    @Select("SELECT id FROM common_knowledge_vec ORDER BY embedding <-> #{embedding}::vector LIMIT #{limit}")
    List<Long> findSimilarCommonKnowledge(@Param("embedding") String embedding, @Param("limit") int limit);

    /**
     * 根据知识库更新向量数据
     * @param id 知识库ID
     * @param embedding 向量数据
     */
    @Update("UPDATE common_knowledge_vec SET embedding = #{embedding}::vector WHERE id = #{id}")
    void updateHouseVec(@Param("id") Long id, @Param("embedding") String embedding);

}