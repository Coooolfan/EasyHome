package com.coooolfan.easyhome.handle;

import com.coooolfan.easyhome.exception.AuthException;
import com.coooolfan.easyhome.exception.BaseException;
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
    public ResponseEntity<String> exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<String> notLogin(AuthException ex){
        log.error("未登录异常：{}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e){
        log.error("系统异常：", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
}
