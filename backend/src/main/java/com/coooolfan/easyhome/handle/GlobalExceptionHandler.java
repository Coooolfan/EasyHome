package com.coooolfan.easyhome.handle;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import com.coooolfan.easyhome.exception.BaseException;
import com.coooolfan.easyhome.exception.RegisterException;
import com.coooolfan.easyhome.response.Code;
import com.coooolfan.easyhome.response.Result;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.security.auth.message.AuthException;
import lombok.extern.slf4j.Slf4j;
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

    @ExceptionHandler(RegisterException.class)
    public Result<String> paramExceptionHandler(RegisterException ex) {
        log.error("参数异常：{}", ex.getMessage());
        return new Result<>(Code.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(NotLoginException.class)
    public Result<String> notLoginExceptionHandler(NotLoginException ex) {
        log.error("token已过期：{}", ex.getMessage());
        return new Result<>(Code.UNAUTHORIZED, "请先登录", null);
    }

    @ExceptionHandler(NotRoleException.class)
    public Result<String> notRoleExceptionHandler(NotRoleException ex) {
        log.error("权限不足：{}", ex.getMessage());
        return new Result<>(Code.FORBIDDEN, "权限不足", null);
    }
}
