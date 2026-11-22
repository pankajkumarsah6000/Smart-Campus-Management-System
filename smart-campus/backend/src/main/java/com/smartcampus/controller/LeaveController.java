package com.smartcampus.controller;

import com.smartcampus.dto.ApiResponse;
import com.smartcampus.dto.request.LeaveRequestCreateRequest;
import com.smartcampus.dto.request.LeaveReviewRequest;
import com.smartcampus.dto.response.LeaveRequestDto;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    public ResponseEntity<ApiResponse<LeaveRequestDto>> apply(@Valid @RequestBody LeaveRequestCreateRequest request,
                                                                @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok("Leave request submitted", leaveService.createLeaveRequest(request, principal.getId())));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<LeaveRequestDto>>> myRequests(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(leaveService.getMyLeaveRequests(principal.getId())));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<ApiResponse<List<LeaveRequestDto>>> pending() {
        return ResponseEntity.ok(ApiResponse.ok(leaveService.getPendingLeaveRequests()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LeaveRequestDto>>> all() {
        return ResponseEntity.ok(ApiResponse.ok(leaveService.getAllLeaveRequests()));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<ApiResponse<LeaveRequestDto>> review(@PathVariable Long id,
                                                                @Valid @RequestBody LeaveReviewRequest request,
                                                                @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok("Leave request reviewed", leaveService.reviewLeaveRequest(id, request, principal.getId())));
    }
}
