package com.smartcampus.service;

import com.smartcampus.dto.response.NotificationDto;
import java.util.List;

public interface NotificationService {
    void notifyUser(Long userId, String title, String message, String type, String linkUrl);
    List<NotificationDto> getMyNotifications(Long userId);
    long getUnreadCount(Long userId);
    void markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);
}
