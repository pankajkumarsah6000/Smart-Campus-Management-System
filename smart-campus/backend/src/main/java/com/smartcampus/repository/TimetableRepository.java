package com.smartcampus.repository;

import com.smartcampus.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    List<Timetable> findBySectionAndSemester(String section, String semester);
    List<Timetable> findByFacultyId(Long facultyId);
}
