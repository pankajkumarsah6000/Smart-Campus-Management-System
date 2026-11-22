package com.smartcampus.service;

import com.smartcampus.dto.request.NoticeRequest;
import com.smartcampus.dto.response.NoticeDto;
import com.smartcampus.entity.Notice;

import java.util.List;

public interface NoticeService {
    NoticeDto createNotice(NoticeRequest request, Long postedByUserId);
    List<NoticeDto> getAllNotices();
    List<NoticeDto> getNoticesForAudience(Notice.TargetAudience audience);
    void deleteNotice(Long id);
}
