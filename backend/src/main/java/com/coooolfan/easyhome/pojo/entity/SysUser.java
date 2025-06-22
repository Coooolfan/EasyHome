package com.coooolfan.easyhome.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author lima
 * @version 0.0.1
 **/
@Data
@AllArgsConstructor
@Schema(name = "SystemUser", description = "用户类")
public class SysUser{
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private final String username;
    private final String password;
    private final String role;
    private final String phoneNumber;
    private final Boolean isEnable;
}
