package com.smartcampus.controller;

import com.smartcampus.dto.ApiResponse;
import com.smartcampus.dto.response.FacultyDashboardDto;
import com.smartcampus.dto.response.FacultyDto;
import com.smartcampus.dto.response.StudentDto;
import com.smartcampus.dto.response.SubjectDto;
import com.smartcampus.dto.response.TimetableSlotDto;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.FacultyService;
import com.smartcampus.service.TimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Faculty portal. Route protection: /api/faculty/** requires ROLE_FACULTY or ROLE_ADMIN
 * (SecurityConfig). Every endpoint resolves data for the *authenticated* faculty member
 * (client-supplied IDs are never trusted for identity).
 */
@RestController
@RequestMapping("/api/faculty")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyService facultyService;
    private final TimetableService timetableService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<FacultyDto>> myProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(facultyService.getFacultyByUserId(principal.getId())));
    }

    @GetMapping("/me/dashboard")
    public ResponseEntity<ApiResponse<FacultyDashboardDto>> myDashboard(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(facultyService.getDashboard(principal.getId())));
    }

    @GetMapping("/me/subjects")
    public ResponseEntity<ApiResponse<List<SubjectDto>>> mySubjects(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(facultyService.getMySubjects(principal.getId())));
    }

    @GetMapping("/me/timetable")
    public ResponseEntity<ApiResponse<List<TimetableSlotDto>>> myTimetable(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(timetableService.getTimetableForFaculty(principal.getId())));
    }

    @GetMapping("/subjects/{subjectId}/students")
    public ResponseEntity<ApiResponse<List<StudentDto>>> studentsForSubject(@PathVariable Long subjectId,
                                                                            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(facultyService.getStudentsForSubject(subjectId, principal.getId())));
    }
}