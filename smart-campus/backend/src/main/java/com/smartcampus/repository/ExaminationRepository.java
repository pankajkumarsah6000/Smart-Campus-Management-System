package com.smartcampus.repository;

import com.smartcampus.entity.Examination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExaminationRepository extends JpaRepository<Examination, Long> {
    List<Examination> findBySubjectId(Long subjectId);
}
