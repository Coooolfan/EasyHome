package com.coooolfan.easyhome.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码
 *
 * @author lima
 * @version 0.0.1
 **/
@Getter
@AllArgsConstructor
public enum Code {
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    INTERNAL_ERROR(500, "系统异常"),
    FORBIDDEN(403, "禁止访问"),;

    private final int code;
    private final String message;
}

