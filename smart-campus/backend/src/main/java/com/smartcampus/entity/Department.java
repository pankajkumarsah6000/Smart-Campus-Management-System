package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "departments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Department extends BaseEntity {

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_of_department_id")
    private Faculty headOfDepartment;
}
