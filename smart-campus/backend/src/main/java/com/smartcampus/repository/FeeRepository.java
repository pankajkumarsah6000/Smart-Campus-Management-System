package com.smartcampus.repository;

import com.smartcampus.entity.Fee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {
    List<Fee> findByStudentId(Long studentId);
    List<Fee> findByStatus(com.smartcampus.entity.Fee.FeeStatus status);
}
