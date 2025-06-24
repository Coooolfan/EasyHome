package com.coooolfan.easyhome.handle;

import com.coooolfan.easyhome.exception.AuthException;
import com.coooolfan.easyhome.exception.BaseException;
import com.coooolfan.easyhome.response.Result;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * 全局异常捕获
 *
 * @author lima
 * @version 0.0.1
 **/
@Slf4j
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex 父类异常
     * @return 结果
     */
    @ExceptionHandler(BaseException.class)
    public Result<String> exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(AuthException.class)
    public Result<String> notLogin(AuthException ex){
        log.error("未登录异常：{}", ex.getMessage());
        return Result.unauthorized(ex.getMessage());
    }
}
