package com.smartcampus.controller;

import com.smartcampus.dto.ApiResponse;
import com.smartcampus.dto.request.AssignmentRequest;
import com.smartcampus.dto.request.AssignmentSubmitRequest;
import com.smartcampus.dto.request.GradeSubmissionRequest;
import com.smartcampus.dto.response.AssignmentDto;
import com.smartcampus.dto.response.SubmissionDto;
import com.smartcampus.entity.RoleName;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    private boolean isAdmin(CustomUserDetails principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + RoleName.ADMIN.name()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<AssignmentDto>> create(@Valid @RequestBody AssignmentRequest request,
                                                             @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok("Assignment created",
                assignmentService.createAssignment(request, principal.getId(), isAdmin(principal))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<AssignmentDto>> update(@PathVariable Long id,
                                                             @Valid @RequestBody AssignmentRequest request,
                                                             @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok("Assignment updated",
                assignmentService.updateAssignment(id, request, principal.getId(), isAdmin(principal))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id,
                                                    @AuthenticationPrincipal CustomUserDetails principal) {
        assignmentService.deleteAssignment(id, principal.getId(), isAdmin(principal));
        return ResponseEntity.ok(ApiResponse.ok("Assignment deleted", null));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<AssignmentDto>>> myAssignments(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(assignmentService.getMyAssignments(principal.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssignmentDto>> detail(@PathVariable Long id,
                                                             @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(assignmentService.getAssignmentDetail(id, principal.getId(), isAdmin(principal))));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<SubmissionDto>> submit(@PathVariable Long id,
                                                             @Valid @RequestBody AssignmentSubmitRequest request,
                                                             @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok("Assignment submitted",
                assignmentService.submitAssignment(id, request, principal.getId())));
    }

    @GetMapping("/{id}/submissions")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<SubmissionDto>>> submissions(@PathVariable Long id,
                                                                        @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                assignmentService.getSubmissionsForAssignment(id, principal.getId(), isAdmin(principal))));
    }

    @GetMapping("/submissions/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<SubmissionDto>>> mySubmissions(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(assignmentService.getMySubmissions(principal.getId())));
    }

    @PutMapping("/submissions/{submissionId}/grade")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<SubmissionDto>> grade(@PathVariable Long submissionId,
                                                            @RequestBody GradeSubmissionRequest request,
                                                            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok("Submission graded",
                assignmentService.gradeSubmission(submissionId, request, principal.getId(), isAdmin(principal))));
    }
}