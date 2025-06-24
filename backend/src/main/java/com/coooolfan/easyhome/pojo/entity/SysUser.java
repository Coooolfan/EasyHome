package com.coooolfan.easyhome.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Builder
@Schema(name = "SystemUser", description = "用户类")
public class SysUser{
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private final String username;
    private final String password;
    private final String role;
    private final String email;
    private final Boolean isEnable;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
