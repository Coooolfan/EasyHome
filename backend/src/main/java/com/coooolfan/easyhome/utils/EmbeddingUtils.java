package com.coooolfan.easyhome.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 向量处理工具类
 * 用于处理embedding向量的长度限制等操作
 */
@Slf4j
public class EmbeddingUtils {

    /**
     * 最大向量长度限制
     */
    public static final int MAX_EMBEDDING_LENGTH = 1536;

    /**
     * 限制float数组的长度，确保不超过最大长度限制
     *
     * @param embedding 原始的float数组
     * @return 长度不超过MAX_EMBEDDING_LENGTH的新float数组
     */
    public static float[] cut(float[] embedding) {
        if (embedding == null) {
            log.warn("输入的embedding数组为null，返回空数组");
            return new float[0];
        }

        if (embedding.length <= MAX_EMBEDDING_LENGTH) {
            log.debug("embedding长度为{}，无需截取", embedding.length);
            return embedding.clone(); // 返回副本，避免修改原数组
        }

        log.info("embedding长度为{}，超过最大限制{}，将截取前{}位",
                embedding.length, MAX_EMBEDDING_LENGTH, MAX_EMBEDDING_LENGTH);

        // 创建新数组并复制前MAX_EMBEDDING_LENGTH个元素
        float[] result = new float[MAX_EMBEDDING_LENGTH];
        System.arraycopy(embedding, 0, result, 0, MAX_EMBEDDING_LENGTH);

        return result;
    }

    /**
     * 检查embedding数组是否超过长度限制
     *
     * @param embedding 要检查的float数组
     * @return true如果长度超过限制，false否则
     */
    public static boolean isEmbeddingTooLong(float[] embedding) {
        return embedding != null && embedding.length > MAX_EMBEDDING_LENGTH;
    }

    /**
     * 获取embedding数组的有效长度（不超过最大限制）
     *
     * @param embedding 要检查的float数组
     * @return 有效长度
     */
    public static int getEffectiveLength(float[] embedding) {
        if (embedding == null) {
            return 0;
        }
        return Math.min(embedding.length, MAX_EMBEDDING_LENGTH);
    }
}
