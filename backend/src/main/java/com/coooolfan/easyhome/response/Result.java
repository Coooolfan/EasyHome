package com.coooolfan.easyhome.response;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 响应类
 *
 * @author lima
 * @version 0.0.1
 **/
@Data
@NoArgsConstructor
public class Result<T> {
    private Code code;
    private String message;
    private T data;
    private Long timestamp;

    public Result(Code code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应，自定义消息，携带数据
     * @param message 消息
     * @param data 数据
     * @return 响应类
     * @param <T> 范型参数
     */
    public static<T> Result<T> ok(String message, T data) {
        return new Result<>(Code.SUCCESS,message, data);
    }

    /**
     * 成功响应，自定义消息
     * @param data 数据
     * @return 响应类
     * @param <T> 范型参数
     */
    public static<T> Result<T> ok(T data) {
        return ok(null, data);
    }


    /**
     * 失败响应，默认消息
     * @param message 消息
     * @return 响应类
     * @param <T> 范型参数
     */
    public static<T> Result<T> error(String message) {
        return new Result<>(Code.INTERNAL_ERROR,message, null);
    }
    public static<T> Result<T> unauthorized(String message) {
        return new Result<>(Code.UNAUTHORIZED, message, null);
    }

    public static <T> Result<T> failed(String message) {
        return new Result<>(Code.FAILED, message, null);
    }
}
