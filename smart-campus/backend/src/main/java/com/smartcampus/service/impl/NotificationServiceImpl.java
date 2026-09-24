package com.smartcampus.service.impl;

import com.smartcampus.dto.response.NotificationDto;
import com.smartcampus.entity.Notification;
import com.smartcampus.entity.User;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.NotificationRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void notifyUser(Long userId, String title, String message, String type, String linkUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(Notification.NotificationType.valueOf(type));
        notification.setLinkUrl(linkUrl);
        notificationRepository.save(notification);
    }

    @Override
    public List<NotificationDto> getMyNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found with id: " + notificationId);
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    private NotificationDto toDto(Notification n) {
        NotificationDto dto = new NotificationDto();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setType(n.getType().name());
        dto.setRead(n.isRead());
        dto.setLinkUrl(n.getLinkUrl());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}
