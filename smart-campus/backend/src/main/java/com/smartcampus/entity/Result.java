package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "results", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"examination_id", "student_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Result extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examination_id", nullable = false)
    private Examination examination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    private Double marksObtained;

    @Column(length = 5)
    private String grade;

    private Boolean passed;

    private String remarks;
}
