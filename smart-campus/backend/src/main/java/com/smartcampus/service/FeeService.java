package com.smartcampus.service;

import com.smartcampus.dto.request.FeePaymentRequest;
import com.smartcampus.dto.request.FeeRecordRequest;
import com.smartcampus.dto.response.FeeDto;

import java.util.List;

public interface FeeService {
    FeeDto createFee(FeeRecordRequest request);
    List<FeeDto> getAllFees();
    List<FeeDto> getFeesForStudent(Long studentUserId);
    List<FeeDto> getFeesForStudentById(Long studentId);
    List<FeeDto> getFeesForParentChild(Long parentUserId, Long studentId);
    FeeDto makePayment(Long feeId, FeePaymentRequest request);
    void deleteFee(Long id);
}