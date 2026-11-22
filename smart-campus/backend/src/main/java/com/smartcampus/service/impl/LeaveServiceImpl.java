package com.smartcampus.service.impl;

import com.smartcampus.dto.request.LeaveRequestCreateRequest;
import com.smartcampus.dto.request.LeaveReviewRequest;
import com.smartcampus.dto.response.LeaveRequestDto;
import com.smartcampus.entity.LeaveRequest;
import com.smartcampus.entity.User;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.LeaveRequestRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.service.LeaveService;
import com.smartcampus.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public LeaveRequestDto createLeaveRequest(LeaveRequestCreateRequest request, Long requestedByUserId) {
        if (request.getToDate().isBefore(request.getFromDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }
        User user = userRepository.findById(requestedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LeaveRequest leave = new LeaveRequest();
        leave.setRequestedBy(user);
        leave.setFromDate(request.getFromDate());
        leave.setToDate(request.getToDate());
        leave.setReason(request.getReason());
        leave.setStatus(LeaveRequest.LeaveStatus.PENDING);

        return toDto(leaveRequestRepository.save(leave));
    }

    @Override
    public List<LeaveRequestDto> getMyLeaveRequests(Long userId) {
        return leaveRequestRepository.findByRequestedById(userId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequestDto> getPendingLeaveRequests() {
        return leaveRequestRepository.findByStatus(LeaveRequest.LeaveStatus.PENDING).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequestDto> getAllLeaveRequests() {
        return leaveRequestRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LeaveRequestDto reviewLeaveRequest(Long id, LeaveReviewRequest request, Long reviewedByUserId) {
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));

        User reviewer = userRepository.findById(reviewedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));

        leave.setStatus(LeaveRequest.LeaveStatus.valueOf(request.getStatus()));
        leave.setReviewedBy(reviewer);
        leave.setReviewComment(request.getComment());
        leave = leaveRequestRepository.save(leave);

        notificationService.notifyUser(
                leave.getRequestedBy().getId(),
                "Leave request " + leave.getStatus().name().toLowerCase(),
                "Your leave request from " + leave.getFromDate() + " to " + leave.getToDate() + " was " + leave.getStatus().name().toLowerCase() + ".",
                "LEAVE",
                "/leave-requests"
        );

        return toDto(leave);
    }

    private LeaveRequestDto toDto(LeaveRequest l) {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setId(l.getId());
        dto.setRequestedByUserId(l.getRequestedBy().getId());
        dto.setRequestedByName(l.getRequestedBy().getFirstName() + " " + l.getRequestedBy().getLastName());
        dto.setFromDate(l.getFromDate());
        dto.setToDate(l.getToDate());
        dto.setReason(l.getReason());
        dto.setStatus(l.getStatus().name());
        if (l.getReviewedBy() != null) {
            dto.setReviewedByName(l.getReviewedBy().getFirstName() + " " + l.getReviewedBy().getLastName());
        }
        dto.setReviewComment(l.getReviewComment());
        dto.setCreatedAt(l.getCreatedAt());
        return dto;
    }
}
