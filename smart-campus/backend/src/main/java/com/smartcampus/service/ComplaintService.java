package com.smartcampus.service;

import com.smartcampus.dto.request.ComplaintRequest;
import com.smartcampus.dto.request.ComplaintResolutionRequest;
import com.smartcampus.dto.response.ComplaintDto;

import java.util.List;

public interface ComplaintService {
    ComplaintDto raiseComplaint(ComplaintRequest request, Long userId);
    List<ComplaintDto> getMyComplaints(Long userId);
    List<ComplaintDto> getAllComplaints();
    ComplaintDto resolveComplaint(Long id, ComplaintResolutionRequest request, Long adminUserId);
}