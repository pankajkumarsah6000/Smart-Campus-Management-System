package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "classrooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Classroom extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String roomNumber;

    @Column(length = 100)
    private String building;

    private Integer capacity;

    @Enumerated(EnumType.STRING)
    private RoomType type = RoomType.LECTURE_HALL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department; // null = shared/common room

    public enum RoomType { LECTURE_HALL, LAB, SEMINAR_ROOM, AUDITORIUM }
}
