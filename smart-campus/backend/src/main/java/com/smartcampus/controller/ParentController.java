package com.smartcampus.controller;

import com.smartcampus.dto.ApiResponse;
import com.smartcampus.dto.response.FeeDto;
import com.smartcampus.dto.response.ParentProfileDto;
import com.smartcampus.dto.response.ResultDto;
import com.smartcampus.dto.response.StudentAttendanceSummaryDto;
import com.smartcampus.dto.response.StudentDashboardDto;
import com.smartcampus.dto.response.StudentDto;
import com.smartcampus.dto.response.TimetableSlotDto;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.ParentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Parent portal. Route protection: /api/parents/** requires ROLE_PARENT or ROLE_ADMIN
 * (SecurityConfig). Every child-student endpoint further enforces that the parent is
 * actually linked to that student (see ParentServiceImpl / AttendanceService).
 */
@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ApiResponse<ParentProfileDto>> myProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(parentService.getMyProfile(principal.getId())));
    }

    @GetMapping("/me/children")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ApiResponse<List<StudentDto>>> myChildren(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(parentService.getMyChildren(principal.getId())));
    }

    @GetMapping("/children/{studentId}/dashboard")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ApiResponse<StudentDashboardDto>> childDashboard(@PathVariable Long studentId,
                                                                           @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(parentService.getChildDashboard(principal.getId(), studentId)));
    }

    @GetMapping("/children/{studentId}/attendance")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ApiResponse<StudentAttendanceSummaryDto>> childAttendance(@PathVariable Long studentId,
                                                                             @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(parentService.getChildAttendance(principal.getId(), studentId)));
    }

    @GetMapping("/children/{studentId}/results")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ApiResponse<List<ResultDto>>> childResults(@PathVariable Long studentId,
                                                                     @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(parentService.getChildResults(principal.getId(), studentId)));
    }

    @GetMapping("/children/{studentId}/fees")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ApiResponse<List<FeeDto>>> childFees(@PathVariable Long studentId,
                                                               @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(parentService.getChildFees(principal.getId(), studentId)));
    }

    @GetMapping("/children/{studentId}/timetable")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ApiResponse<List<TimetableSlotDto>>> childTimetable(@PathVariable Long studentId,
                                                                              @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(parentService.getChildTimetable(principal.getId(), studentId)));
    }
}