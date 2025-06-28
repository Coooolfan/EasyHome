package com.coooolfan.easyhome.pojo.dto;

import lombok.Data;

/**
 * @author lima
 * @version 0.0.1
 **/
@Data
public class UserQueryDTO {
    private final String username;
    private final String role;
    private final String phone;
    private final String email;
    private final Boolean isEnable;
}
