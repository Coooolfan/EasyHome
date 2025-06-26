package com.coooolfan.easyhome.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author lima
 * @version 0.0.1
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("user_favorites")
public class UserFavorite {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long houseId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
