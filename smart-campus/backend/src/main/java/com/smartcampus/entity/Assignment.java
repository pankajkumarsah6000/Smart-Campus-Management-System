package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Assignment extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_faculty_id", nullable = false)
    private Faculty createdBy;

    private LocalDateTime dueDate;

    private Integer maxMarks;

    private String attachmentUrl;
}
