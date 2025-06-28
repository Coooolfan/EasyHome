package com.coooolfan.easyhome.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.coooolfan.easyhome.pojo.entity.HouseRecord;
import com.coooolfan.easyhome.pojo.entity.ReviewResult;
import com.coooolfan.easyhome.response.Result;
import com.coooolfan.easyhome.service.AppointmentService;
import com.coooolfan.easyhome.service.HouseRecordService;
import com.coooolfan.easyhome.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
@RestController
@RequestMapping("/api/admin/review")
@Tag(name = "审核接口")
@AllArgsConstructor
//@SaCheckRole("super_admin")
public class ReviewController {

    @Resource
    private HouseRecordService houseRecordService;

    @Resource
    private AppointmentService appointmentService;

    @Resource
    private NotificationService notificationService;

    @GetMapping("/house/received")
    public Result<List<HouseRecord>> getReceived(){
        return Result.ok(houseRecordService.getReceivedRecords());
    }

    @PostMapping("/house/approve")
    public Result<String> approve(@RequestParam Long id,
                                  @RequestParam Boolean pass,
                                  @RequestParam(required = false) String reason) {
        if(houseRecordService.review(id, pass, reason)) {
            notificationService.send(
                    houseRecordService.getUserIdByRecordId(id),
                    "房屋审核通过",
                    "您的房屋信息已通过审核");
        }else {
            notificationService.send(
                houseRecordService.getUserIdByRecordId(id),
                "房屋审核未通过",
                reason == null ? "您的房屋信息未通过审核" : reason);
        }
        return Result.ok("处理完成");
    }

    @PostMapping("/appointment/approve")
    public Result<String> approveView(@RequestParam Long id,
                                      @RequestParam Boolean pass,
                                      @RequestParam(required = false) String reason) {
        ReviewResult result = appointmentService.review(id, pass, reason);

        if (result.isApproved()) {
            notificationService.send(
                    result.getUserId(),
                    "预约审核通过",
                            "房屋：" + result.getHouseName() + " 的预约申请已通过审核\n" +
                    "房主联系方式为：" + result.getPhone()
            );
        } else {
            notificationService.send(
                    result.getUserId(),
                    "预约未通过",
                    reason == null ? "您的预约申请未通过审核" : reason
            );
        }

        return Result.ok("处理完成");
    }

}

