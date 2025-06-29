package com.coooolfan.easyhome.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.coooolfan.easyhome.exception.BussinessException;
import com.coooolfan.easyhome.message.AppointmentProducer;
import com.coooolfan.easyhome.pojo.dto.AppointmentDTO;
import com.coooolfan.easyhome.pojo.entity.AppointmentRecord;
import com.coooolfan.easyhome.response.Result;
import com.coooolfan.easyhome.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户提交预约请求接口
 * @author lima
 */
@Slf4j
@RestController
@Tag(name = "预约接口组")
@RequestMapping("/api/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentProducer appointmentProducer;
    private final AppointmentService appointmentService;

    @PostMapping("/submit")
    @Operation(summary = "提交预约请求")
    public Result<Void> submit(@RequestBody AppointmentDTO dto) {

        QueryWrapper<AppointmentRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", dto.getName());
        queryWrapper.eq("phone", dto.getPhone());
        long count = appointmentService.count(queryWrapper);
        if (count > 5) {
            throw new BussinessException("预约数量超过限制，请稍后再试");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        dto.setUserId(userId);

        appointmentProducer.send(dto);
        return Result.ok(null);
    }

    @GetMapping("list")
    @Operation(summary = "获取用户预约列表")
    public Result<List<AppointmentRecord>> list() {
        List<AppointmentRecord> appointments = appointmentService.getAppointments();
        return Result.ok(appointments);
    }
}
