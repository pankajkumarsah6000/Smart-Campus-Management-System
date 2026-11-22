package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "ai_conversations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AIConversation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String userMessage;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String aiResponse;

    @Column(length = 50)
    private String intent; // e.g. TIMETABLE_QUERY, ATTENDANCE_QUERY, STUDY_PLAN

    @Column(length = 50)
    private String sessionId;
}
