package com.smartcampus.controller;

import com.smartcampus.dto.ApiResponse;
import com.smartcampus.dto.response.StudentDashboardDto;
import com.smartcampus.dto.response.StudentDto;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints for the currently authenticated student to access their own data.
 * Route protection: /api/students/me/** requires ROLE_STUDENT or ROLE_ADMIN (SecurityConfig).
 */
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StudentDto>> getMyProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getStudentByUserId(principal.getId())));
    }

    @GetMapping("/me/dashboard")
    public ResponseEntity<ApiResponse<StudentDashboardDto>> getMyDashboard(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getDashboard(principal.getId())));
    }
}
