package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "submissions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"assignment_id", "student_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Submission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    private String fileUrl;

    @Column(columnDefinition = "TEXT")
    private String textAnswer;

    private LocalDateTime submittedAt;

    private Double marksAwarded;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    public enum SubmissionStatus { SUBMITTED, LATE, EVALUATED }
}
