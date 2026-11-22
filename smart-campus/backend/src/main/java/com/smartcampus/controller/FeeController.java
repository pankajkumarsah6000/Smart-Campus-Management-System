package com.smartcampus.controller;

import com.smartcampus.dto.ApiResponse;
import com.smartcampus.dto.request.FeePaymentRequest;
import com.smartcampus.dto.request.FeeRecordRequest;
import com.smartcampus.dto.response.FeeDto;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.FeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
public class FeeController {

    private final FeeService feeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeeDto>> create(@Valid @RequestBody FeeRecordRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Fee record created", feeService.createFee(request)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FeeDto>>> all() {
        return ResponseEntity.ok(ApiResponse.ok(feeService.getAllFees()));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<FeeDto>>> myFees(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(feeService.getFeesForStudent(principal.getId())));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FeeDto>>> forStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.ok(feeService.getFeesForStudentById(studentId)));
    }

    @PostMapping("/{feeId}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeeDto>> pay(@PathVariable Long feeId,
                                                   @Valid @RequestBody FeePaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Payment recorded", feeService.makePayment(feeId, request)));
    }

    @DeleteMapping("/{feeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long feeId) {
        feeService.deleteFee(feeId);
        return ResponseEntity.ok(ApiResponse.ok("Fee record deleted", null));
    }
}