package com.smartcampus.repository;

import com.smartcampus.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findAllByOrderByCreatedAtDesc();
    List<Notice> findByTargetAudienceOrderByCreatedAtDesc(com.smartcampus.entity.Notice.TargetAudience audience);
}
