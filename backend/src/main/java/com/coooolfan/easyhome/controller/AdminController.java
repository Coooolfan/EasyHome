package com.coooolfan.easyhome.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.coooolfan.easyhome.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin")
@Tag(name = "管理员接口组")
public class AdminController {

    private AdminService adminService;

//    @SaCheckRole("admin_user")
    @PostMapping("/house_vec/setup")
    @Operation(summary = "初始化房屋向量数据")
    public void setupHouseVec() {
        adminService.resetHouseVec();
    }
}
