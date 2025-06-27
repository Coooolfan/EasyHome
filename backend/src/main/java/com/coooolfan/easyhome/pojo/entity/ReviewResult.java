package com.coooolfan.easyhome.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lima
 * @version 0.0.1
 * 审核预约后返回的信息，包括审核是否通过、预约者ID和房主联系方式。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResult {
    /**
     * 是否通过审核
     */
    private boolean approved;

    /**
     * 提交预约的用户ID（用于推送通知）
     */
    private Long userId;

    /**
     * 房主联系方式（只有在审核通过时返回）
     */
    private String phone;


    private String houseName;
}

