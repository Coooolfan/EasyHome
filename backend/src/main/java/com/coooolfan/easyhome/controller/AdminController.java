package com.coooolfan.easyhome.controller;

import com.coooolfan.easyhome.service.AdminService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private AdminService adminService;

    @PostMapping("/house_vec/setup")
    public void setupHouseVec() {
        adminService.resetHouseVec();
    }
}
