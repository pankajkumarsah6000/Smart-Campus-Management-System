package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fees")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Fee extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(length = 100)
    private String feeType; // Tuition, Hostel, Exam, Library ...

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(precision = 12, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    private LocalDate dueDate;

    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    private FeeStatus status = FeeStatus.PENDING;

    @Column(length = 20)
    private String semester;

    public enum FeeStatus { PENDING, PARTIAL, PAID, OVERDUE }
}
