package com.smartcampus.controller;

import com.smartcampus.dto.ApiResponse;
import com.smartcampus.dto.request.MarkAttendanceRequest;
import com.smartcampus.dto.response.AttendanceRecordDto;
import com.smartcampus.dto.response.StudentAttendanceSummaryDto;
import com.smartcampus.entity.RoleName;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/mark")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceRecordDto>>> markAttendance(
            @Valid @RequestBody MarkAttendanceRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        boolean admin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + RoleName.ADMIN.name()));
        return ResponseEntity.ok(ApiResponse.ok("Attendance recorded",
                attendanceService.markAttendance(request, principal.getId(), admin)));
    }

    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceRecordDto>>> getForSubjectAndDate(
            @PathVariable Long subjectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getAttendanceForSubjectAndDate(subjectId, date)));
    }

    @GetMapping("/me/summary")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentAttendanceSummaryDto>> getMySummary(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getSummaryForStudentByUserId(principal.getId())));
    }

    @GetMapping("/student/{studentId}/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'PARENT')")
    public ResponseEntity<ApiResponse<StudentAttendanceSummaryDto>> getStudentSummary(
            @PathVariable Long studentId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long parentUserId = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + RoleName.PARENT.name()))
                ? principal.getId() : null;
        return ResponseEntity.ok(ApiResponse.ok(
                attendanceService.getSummaryForStudentChecked(studentId, parentUserId)));
    }
}
