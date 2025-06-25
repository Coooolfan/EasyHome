package com.coooolfan.easyhome.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.coooolfan.easyhome.response.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lima
 * @version 0.0.1
 **/
@RestController
public class AuthDemoController {
    @GetMapping("/api/demo/check")
    public void check(){
        // 获取：当前账号所拥有的角色集合
        System.out.println(StpUtil.getRoleList());

        // 判断：当前账号是否拥有指定角色, 返回 true 或 false
        System.out.println(StpUtil.hasRole("super_admin"));
    }

    @SaCheckRole("super_admin")
    @GetMapping("/api/demo/admin")
    public void admin(){
        System.out.println("当前账号拥有 super_admin 角色");
    }
}
