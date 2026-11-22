package com.smartcampus.service.impl;

import com.smartcampus.dto.request.NoticeRequest;
import com.smartcampus.dto.response.NoticeDto;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Notice;
import com.smartcampus.entity.User;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.NoticeRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public NoticeDto createNotice(NoticeRequest request, Long postedByUserId) {
        User postedBy = userRepository.findById(postedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + postedByUserId));

        Notice notice = new Notice();
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setPostedBy(postedBy);
        notice.setTargetAudience(request.getTargetAudience() != null
                ? Notice.TargetAudience.valueOf(request.getTargetAudience()) : Notice.TargetAudience.ALL);
        notice.setPriority(request.getPriority() != null
                ? Notice.Priority.valueOf(request.getPriority()) : Notice.Priority.NORMAL);
        notice.setExpiresAt(request.getExpiresAt());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));
            notice.setDepartment(dept);
        }

        return toDto(noticeRepository.save(notice));
    }

    @Override
    public List<NoticeDto> getAllNotices() {
        return noticeRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<NoticeDto> getNoticesForAudience(Notice.TargetAudience audience) {
        return noticeRepository.findByTargetAudienceOrderByCreatedAtDesc(audience).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found with id: " + id));
        noticeRepository.delete(notice);
    }

    private NoticeDto toDto(Notice n) {
        NoticeDto dto = new NoticeDto();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setContent(n.getContent());
        dto.setPostedByName(n.getPostedBy().getFirstName() + " " + n.getPostedBy().getLastName());
        dto.setTargetAudience(n.getTargetAudience().name());
        dto.setPriority(n.getPriority().name());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}
