package com.coooolfan.easyhome.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.coooolfan.easyhome.pojo.dto.LoginDTO;
import com.coooolfan.easyhome.pojo.dto.ProfileDTO;
import com.coooolfan.easyhome.pojo.dto.RegisterDTO;
import com.coooolfan.easyhome.pojo.entity.UserNotification;
import com.coooolfan.easyhome.pojo.vo.UserVO;
import com.coooolfan.easyhome.response.Result;
import com.coooolfan.easyhome.service.NotificationService;
import com.coooolfan.easyhome.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.security.auth.message.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
@Slf4j
@Tag(name = "用户接口组")
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    private SysUserService sysUserService;

    @Resource
    private NotificationService notificationService;

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

    @SaCheckLogin
    @GetMapping("/getUserInfo")
    @Operation(summary = "获取用户信息")
    public Result<UserVO> getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("获取用户信息：{}", userId);
        UserVO userVO = sysUserService.getUserInfo(userId);
        return Result.ok(userVO);
    }

    @SaCheckLogin
    @PutMapping("/updateProfile")
    @Operation(summary = "更新用户信息")
    public Result<String> updateProfile(@Validated @RequestBody ProfileDTO profileDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("更新用户信息：{}", profileDTO);
        sysUserService.updateProfileById(userId, profileDTO);
        return Result.ok("更新成功");
    }

    @SaCheckLogin
    @PutMapping("/updatePassword")
    @Operation(summary = "更新用户密码")
    public Result<String> updatePassword(@RequestParam String oldPassword,
                                         @RequestParam String newPassword) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("更新用户密码：{}", userId);
        sysUserService.updatePasswordById(userId, oldPassword, newPassword);
        return Result.ok("密码更新成功");
    }

    @SaCheckLogin
    @DeleteMapping("/delete")
    @Operation(summary = "删除用户账号")
    public Result<String> deleteUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("删除用户账号：{}", userId);
        sysUserService.removeById(userId);
        StpUtil.logout();
        return Result.ok("账号已删除");
    }
}
