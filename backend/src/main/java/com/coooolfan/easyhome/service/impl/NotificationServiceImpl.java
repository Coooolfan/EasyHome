package com.coooolfan.easyhome.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coooolfan.easyhome.service.NotificationService;
import com.coooolfan.easyhome.mapper.UserNotificationMapper;
import com.coooolfan.easyhome.pojo.entity.UserNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl
        extends ServiceImpl<UserNotificationMapper, UserNotification>
        implements NotificationService {

    private final UserNotificationMapper notificationMapper;

    @Override
    public void send(Long userId, String title, String content) {
        UserNotification notification = UserNotification.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .isRead(false)
                .build();
        notificationMapper.insert(notification);
    }

    @Override
    public List<UserNotification> getByUserId(Long userId) {
        return this.list(Wrappers.<UserNotification>lambdaQuery().eq(UserNotification::getUserId, userId));
    }
}
