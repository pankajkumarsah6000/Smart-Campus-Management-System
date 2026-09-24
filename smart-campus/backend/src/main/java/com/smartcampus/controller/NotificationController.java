package com.smartcampus.controller;

import com.smartcampus.dto.ApiResponse;
import com.smartcampus.dto.response.NotificationDto;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> myNotifications(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getMyNotifications(principal.getId())));
    }

    @GetMapping("/me/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unreadCount(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(Map.of("count", notificationService.getUnreadCount(principal.getId()))));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        notificationService.markAsRead(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Marked as read", null));
    }

    @PutMapping("/me/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(@AuthenticationPrincipal CustomUserDetails principal) {
        notificationService.markAllAsRead(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("All marked as read", null));
    }
}
