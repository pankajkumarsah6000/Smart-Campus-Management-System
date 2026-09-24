package com.smartcampus.controller;

import com.smartcampus.dto.ApiResponse;
import com.smartcampus.dto.request.ComplaintRequest;
import com.smartcampus.dto.request.ComplaintResolutionRequest;
import com.smartcampus.dto.response.ComplaintDto;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    public ResponseEntity<ApiResponse<ComplaintDto>> raise(@Valid @RequestBody ComplaintRequest request,
                                                           @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok("Complaint submitted", complaintService.raiseComplaint(request, principal.getId())));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<ComplaintDto>>> myComplaints(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(complaintService.getMyComplaints(principal.getId())));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ComplaintDto>>> all() {
        return ResponseEntity.ok(ApiResponse.ok(complaintService.getAllComplaints()));
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintDto>> resolve(@PathVariable Long id,
                                                             @Valid @RequestBody ComplaintResolutionRequest request,
                                                             @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok("Complaint updated",
                complaintService.resolveComplaint(id, request, principal.getId())));
    }
}