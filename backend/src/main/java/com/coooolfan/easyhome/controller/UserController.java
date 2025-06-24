package com.coooolfan.easyhome.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.coooolfan.easyhome.pojo.dto.LoginDTO;
import com.coooolfan.easyhome.pojo.dto.RegisterDTO;
import com.coooolfan.easyhome.response.Result;
import com.coooolfan.easyhome.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.security.auth.message.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lima
 * @version 0.0.1
 **/
@Slf4j
@Tag(name = "鉴权接口组")
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    private SysUserService sysUserService;

    @PostMapping("/login")
    @Operation(summary = "登录接口")
    public Result<String> doLogin(@Validated @RequestBody LoginDTO loginDTO) throws AuthException {
        log.info("用户登录：{}",loginDTO.getUsername());
        Long userId = sysUserService.login(loginDTO);
        if (userId != null) {
            StpUtil.login(userId);
        }
        return Result.ok(StpUtil.getTokenValue());
    }

    @PostMapping("/logout")
    @Operation(summary = "退出接口")
    public Result<String> doLogout(){
        log.info("是否登录：{}", StpUtil.isLogin());
        StpUtil.logout();
        return Result.ok(null);
    }

    @PostMapping("/register")
    @Operation(summary = "注册接口")
    public Result<String> doRegister(@Validated @RequestBody RegisterDTO registerDTO){
        log.info("用户注册：{}", registerDTO.getUsername());
        sysUserService.register(registerDTO);
        return Result.ok("注册成功");
    }
}
