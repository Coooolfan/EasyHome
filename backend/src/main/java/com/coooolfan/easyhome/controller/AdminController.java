package com.coooolfan.easyhome.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coooolfan.easyhome.mapper.SysUserMapper;
import com.coooolfan.easyhome.pojo.dto.LoginDTO;
import com.coooolfan.easyhome.pojo.dto.UserQueryDTO;
import com.coooolfan.easyhome.pojo.entity.House;
import com.coooolfan.easyhome.pojo.entity.SysUser;
import com.coooolfan.easyhome.response.Result;
import com.coooolfan.easyhome.service.AdminService;
import com.coooolfan.easyhome.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.security.auth.message.AuthException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/admin")
@Tag(name = "管理员接口组")
public class AdminController {

    private AdminService adminService;

    private SysUserService sysUserService;

//    @SaCheckRole("admin_user")
    @PostMapping("/house_vec/setup")
    @Operation(summary = "初始化房屋向量数据")
    public void setupHouseVec() {
        adminService.resetHouseVec();
    }

    @PostMapping("/login")
    @Operation(summary = "管理员登录接口")
    public Result<String> doLogin(@Validated @RequestBody LoginDTO loginDTO) throws AuthException {
        log.info("用户登录：{}",loginDTO.getUsername());
        Long userId = sysUserService.login(loginDTO);
        if (userId != null) {
            StpUtil.login(userId);
        }
        if (!StpUtil.hasRole("role_admin")){
            throw new AuthException("权限不足");
        }
        return Result.ok(StpUtil.getTokenValue());
    }

    @PostMapping("/logout")
    @Operation(summary = "管理员退出接口")
    public Result<String> doLogout(){
        log.info("是否登录：{}", StpUtil.isLogin());
        StpUtil.logout();
        return Result.ok(null);
    }

    @GetMapping("/users")
    @Operation(summary = "分页获取所有用户信息")
    @SaCheckRole("role_admin")
    public Result<List<SysUser>> getUsers(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          UserQueryDTO userQueryDTO) {
        log.info("获取所有用户信息");
        Page<SysUser> page = new Page<>(current, size);
        List<SysUser> users = sysUserService.getUsersByPage(page, userQueryDTO);
        return Result.ok(users);
    }




}
