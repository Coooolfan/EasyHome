package com.coooolfan.easyhome.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author lima
 * @version 0.0.1
 **/
@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
    @NotBlank(message = "邮箱不能为空")
    private String email;
}
