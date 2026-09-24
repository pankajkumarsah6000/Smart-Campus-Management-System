package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Notice extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by_user_id", nullable = false)
    private User postedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TargetAudience targetAudience;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department; // null = campus-wide

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.NORMAL;

    private LocalDateTime expiresAt;

    public enum TargetAudience { ALL, STUDENTS, FACULTY, PARENTS }
    public enum Priority { LOW, NORMAL, HIGH, URGENT }
}
