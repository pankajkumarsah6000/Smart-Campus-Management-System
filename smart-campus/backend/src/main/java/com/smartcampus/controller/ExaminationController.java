package com.smartcampus.controller;

import com.smartcampus.dto.ApiResponse;
import com.smartcampus.dto.request.ExaminationRequest;
import com.smartcampus.dto.request.ResultRequest;
import com.smartcampus.dto.response.ExaminationDto;
import com.smartcampus.dto.response.ResultDto;
import com.smartcampus.entity.RoleName;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.ExaminationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExaminationController {

    private final ExaminationService examinationService;

    private boolean isAdmin(CustomUserDetails principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + RoleName.ADMIN.name()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<ExaminationDto>> create(@Valid @RequestBody ExaminationRequest request,
                                                              @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok("Examination created",
                examinationService.createExamination(request, principal.getId(), isAdmin(principal))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<ExaminationDto>> update(@PathVariable Long id,
                                                              @Valid @RequestBody ExaminationRequest request,
                                                              @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok("Examination updated",
                examinationService.updateExamination(id, request, principal.getId(), isAdmin(principal))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id,
                                                    @AuthenticationPrincipal CustomUserDetails principal) {
        examinationService.deleteExamination(id, principal.getId(), isAdmin(principal));
        return ResponseEntity.ok(ApiResponse.ok("Examination deleted", null));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<ApiResponse<List<ExaminationDto>>> list(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                examinationService.getAllExaminations(principal.getId(), isAdmin(principal))));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<ExaminationDto>>> myExams(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(examinationService.getExaminationsForStudent(principal.getId())));
    }

    @GetMapping("/next")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ExaminationDto>> nextExam(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(examinationService.getNextExamForStudent(principal.getId())));
    }

    @GetMapping("/results/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<ResultDto>>> myResults(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(examinationService.getResultsForStudent(principal.getId())));
    }

    @GetMapping("/{id}/results")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ResultDto>>> resultsForExam(@PathVariable Long id,
                                                                       @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                examinationService.getResultsByExamination(id, principal.getId(), isAdmin(principal))));
    }

    @PostMapping("/{id}/results")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<ResultDto>> recordResult(@PathVariable Long id,
                                                               @Valid @RequestBody ResultRequest request,
                                                               @AuthenticationPrincipal CustomUserDetails principal) {
        request.setExaminationId(id);
        return ResponseEntity.ok(ApiResponse.ok("Result recorded",
                examinationService.recordResult(request, principal.getId(), isAdmin(principal))));
    }

    @PutMapping("/results/{resultId}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<ResultDto>> updateResult(@PathVariable Long resultId,
                                                               @Valid @RequestBody ResultRequest request,
                                                               @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok("Result updated",
                examinationService.updateResult(resultId, request, principal.getId(), isAdmin(principal))));
    }
}