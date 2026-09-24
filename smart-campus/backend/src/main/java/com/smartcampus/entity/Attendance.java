package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "attendance", indexes = {
        @Index(name = "idx_attendance_student_date", columnList = "student_id, attendance_date")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "subject_id", "attendance_date"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Attendance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marked_by_faculty_id")
    private Faculty markedBy;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    private String remarks;

    public enum AttendanceStatus { PRESENT, ABSENT, LATE, EXCUSED }
}
