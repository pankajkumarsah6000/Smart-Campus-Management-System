package com.smartcampus.service;

import com.smartcampus.dto.request.LeaveRequestCreateRequest;
import com.smartcampus.dto.request.LeaveReviewRequest;
import com.smartcampus.dto.response.LeaveRequestDto;
import java.util.List;

public interface LeaveService {
    LeaveRequestDto createLeaveRequest(LeaveRequestCreateRequest request, Long requestedByUserId);
    List<LeaveRequestDto> getMyLeaveRequests(Long userId);
    List<LeaveRequestDto> getPendingLeaveRequests();
    List<LeaveRequestDto> getAllLeaveRequests();
    LeaveRequestDto reviewLeaveRequest(Long id, LeaveReviewRequest request, Long reviewedByUserId);
}
