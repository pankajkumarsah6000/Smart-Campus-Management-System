package com.smartcampus.repository;

import com.smartcampus.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findByStudentId(Long studentId);
    List<Result> findByExaminationId(Long examinationId);
    Optional<Result> findByExaminationIdAndStudentId(Long examinationId, Long studentId);
}
