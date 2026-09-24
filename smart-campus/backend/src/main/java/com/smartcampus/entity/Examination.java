package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "examinations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Examination extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name; // "Mid-Term", "Final", "Unit Test 1"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    private LocalDateTime examDate;

    private Integer durationMinutes;

    private Integer maxMarks;

    @Column(length = 100)
    private String venue;
}
