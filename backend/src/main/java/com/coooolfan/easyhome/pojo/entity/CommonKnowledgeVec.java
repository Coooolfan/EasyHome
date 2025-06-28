package com.coooolfan.easyhome.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公共知识向量实体类，对应common_knowledge_vec表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("common_knowledge_vec")
public class CommonKnowledgeVec {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("content")
    private String content;

    @TableField("embedding")
    private float[] embedding;
} 