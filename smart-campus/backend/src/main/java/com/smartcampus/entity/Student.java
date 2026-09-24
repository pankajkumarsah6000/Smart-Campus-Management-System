package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "students")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Student extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 30)
    private String rollNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(length = 20)
    private String semester;

    @Column(length = 20)
    private String section;

    private LocalDate dateOfBirth;

    private LocalDate admissionDate;

    @Enumerated(EnumType.STRING)
    private StudentStatus status = StudentStatus.ACTIVE;

    public enum StudentStatus { ACTIVE, GRADUATED, SUSPENDED, INACTIVE }
}
