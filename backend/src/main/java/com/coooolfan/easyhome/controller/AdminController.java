package com.coooolfan.easyhome.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coooolfan.easyhome.mapper.SysUserMapper;
import com.coooolfan.easyhome.pojo.dto.LoginDTO;
import com.coooolfan.easyhome.pojo.dto.UserCreateDTO;
import com.coooolfan.easyhome.pojo.dto.UserQueryDTO;
import com.coooolfan.easyhome.pojo.dto.UserUpdateDTO;
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

    @PostMapping("/users")
    @Operation(summary = "创建用户")
    @SaCheckRole("role_admin")
    public Result<String> createUser(@Validated @RequestBody UserCreateDTO userCreateDTO) {
        log.info("创建用户：{}", userCreateDTO.getUsername());
        sysUserService.createUser(userCreateDTO);

        return Result.ok("用户创建成功");

    }

    @PutMapping("/users/{id}")
    @Operation(summary = "更新用户信息")
    @SaCheckRole("role_admin")
    public Result<String> updateUser(@PathVariable Long id, @Validated @RequestBody UserUpdateDTO userUpdateDTO) {
        log.info("更新用户信息，用户ID：{}", id);
        sysUserService.updateUser(id, userUpdateDTO);

        return Result.ok("用户信息更新成功");

    }

    @PutMapping("/users/{id}/status")
    @Operation(summary = "修改用户状态")
    @SaCheckRole("role_admin")
    public Result<String> updateUserStatus(@PathVariable Long id, @RequestParam Boolean isEnable) {
        log.info("修改用户状态，用户ID：{}，启用状态：{}", id, isEnable);
        sysUserService.updateUserStatus(id, isEnable);
        return Result.ok("用户状态修改成功");

    }

    @PostMapping("/common_knowledge_vec/setup")
    @Operation(summary = "初始化公共知识向量数据")
    public void setupCommonKnowledgeVec() {
        adminService.resetCommonKnowledgeVec();
    }
}