package com.coooolfan.easyhome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coooolfan.easyhome.pojo.entity.UserNotification;

import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
public interface NotificationService extends IService<UserNotification> {
    void send(Long userId, String title, String content);

    List<UserNotification> getByUserId(Long userId);
}
