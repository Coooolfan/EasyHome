package com.coooolfan.easyhome.controller;

import com.coooolfan.easyhome.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

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


    @PostMapping("/common_knowledge_vec/setup")
    @Operation(summary = "初始化公共知识向量数据")
    public void setupCommonKnowledgeVec() {
        adminService.resetCommonKnowledgeVec();
    }


}
