package com.coooolfan.easyhome.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.coooolfan.easyhome.pojo.entity.UserNotification;
import com.coooolfan.easyhome.response.Result;
import com.coooolfan.easyhome.service.NotificationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
@RestController
@Slf4j
@RequestMapping("/api/notification")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @SaCheckLogin
    @GetMapping("/unread")
    public Result<List<UserNotification>> getUnreadNotifications() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<UserNotification> unreadNotifications = notificationService.list(
                new QueryWrapper<UserNotification>()
                        .eq("user_id", userId)
                        .eq("is_read", false)
        );
        return Result.ok(unreadNotifications);
    }


    @SaCheckLogin
    @GetMapping
    public Result<List<UserNotification>> getMyNotifications() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<UserNotification> list = notificationService.getByUserId(userId);
        return Result.ok(list);
    }

    @SaCheckLogin
    @DeleteMapping
    public Result<String> deleteNotification(@RequestParam Long id) {
        log.info("删除通知：{}", id);
        notificationService.remove(new QueryWrapper<UserNotification>().eq("id", id));
        return Result.ok("通知已删除");
    }

    @SaCheckLogin
    @PutMapping
    public Result<String> changeNotificationStatus(@RequestParam Long id) {
        log.info("修改通知状态：{}", id);
        UserNotification notification = notificationService.getById(id);
        if (notification != null) {
            notification.setIsRead(true);
            notificationService.updateById(notification);
            return Result.ok("通知状态已更新");
        } else {
            return Result.error("通知不存在");
        }
    }

    @SaCheckLogin
    @PutMapping("/all")
    public Result<String> changeAllNotificationsStatus() {
        log.info("修改所有通知状态");
        Long userId = StpUtil.getLoginIdAsLong();
        List<UserNotification> notifications = notificationService.getByUserId(userId);
        for (UserNotification notification : notifications) {
            notification.setIsRead(true);
            notificationService.updateById(notification);
        }
        return Result.ok("所有通知状态已更新");
    }

    @SaCheckLogin
    @GetMapping("/all_unread")
    public Result<List<UserNotification>> getAllUnreadNotifications() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<UserNotification> unreadNotifications = notificationService.list(
                new QueryWrapper<UserNotification>()
                        .eq("user_id", userId)
                        .eq("is_read", false)
        );
        return Result.ok(unreadNotifications);
    }
}
