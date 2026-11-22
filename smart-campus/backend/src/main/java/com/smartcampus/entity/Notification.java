package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_user_read", columnList = "user_id, is_read")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    private String linkUrl;

    public enum NotificationType {
        ASSIGNMENT, ATTENDANCE, EXAMINATION, RESULT, FEE, NOTICE, LEAVE, ANNOUNCEMENT, COMPLAINT
    }
}
