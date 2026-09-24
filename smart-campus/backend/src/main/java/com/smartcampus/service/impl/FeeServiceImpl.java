package com.smartcampus.service.impl;

import com.smartcampus.dto.request.FeePaymentRequest;
import com.smartcampus.dto.request.FeeRecordRequest;
import com.smartcampus.dto.response.FeeDto;
import com.smartcampus.entity.Fee;
import com.smartcampus.entity.Parent;
import com.smartcampus.entity.Student;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.UnauthorizedException;
import com.smartcampus.repository.FeeRepository;
import com.smartcampus.repository.ParentRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.service.FeeService;
import com.smartcampus.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeeServiceImpl implements FeeService {

    private final FeeRepository feeRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public FeeDto createFee(FeeRecordRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));

        Fee fee = new Fee();
        fee.setStudent(student);
        fee.setFeeType(request.getFeeType());
        fee.setAmount(request.getAmount());
        fee.setAmountPaid(BigDecimal.ZERO);
        fee.setDueDate(request.getDueDate());
        fee.setSemester(request.getSemester());
        refreshStatus(fee);
        return toDto(feeRepository.save(fee));
    }

    @Override
    public List<FeeDto> getAllFees() {
        return feeRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<FeeDto> getFeesForStudent(Long studentUserId) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No student profile linked to this account"));
        return getFeesForStudentById(student.getId());
    }

    @Override
    public List<FeeDto> getFeesForStudentById(Long studentId) {
        return feeRepository.findByStudentId(studentId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<FeeDto> getFeesForParentChild(Long parentUserId, Long studentId) {
        Parent parent = parentRepository.findByUserId(parentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No parent profile linked to this account"));
        boolean linked = parent.getChildren().stream().anyMatch(c -> c.getId().equals(studentId));
        if (!linked) {
            throw new UnauthorizedException("You can only view fees for your linked children");
        }
        return getFeesForStudentById(studentId);
    }

    @Override
    @Transactional
    public FeeDto makePayment(Long feeId, FeePaymentRequest request) {
        Fee fee = feeRepository.findById(feeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee record not found with id: " + feeId));

        BigDecimal newPaid = fee.getAmountPaid().add(request.getAmountPaid());
        if (newPaid.compareTo(fee.getAmount()) > 0) {
            throw new BadRequestException("Payment exceeds the total due amount");
        }
        fee.setAmountPaid(newPaid);
        if (newPaid.compareTo(fee.getAmount()) >= 0) {
            fee.setPaidDate(LocalDate.now());
        }
        refreshStatus(fee);
        fee = feeRepository.save(fee);

        if (fee.getStatus() == Fee.FeeStatus.PAID) {
            notificationService.notifyUser(
                    fee.getStudent().getUser().getId(),
                    "Fee payment complete",
                    "Your " + (fee.getFeeType() == null ? "" : fee.getFeeType() + " ") + "fee is fully paid. Thank you!",
                    "FEE",
                    "/student/fees"
            );
        }
        return toDto(fee);
    }

    @Override
    @Transactional
    public void deleteFee(Long id) {
        Fee fee = feeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee record not found with id: " + id));
        feeRepository.delete(fee);
    }

    private void refreshStatus(Fee fee) {
        BigDecimal zero = BigDecimal.ZERO;
        if (fee.getAmountPaid() == null) fee.setAmountPaid(zero);
        if (fee.getAmount().compareTo(fee.getAmountPaid()) <= 0) {
            fee.setStatus(Fee.FeeStatus.PAID);
        } else if (fee.getAmountPaid().compareTo(zero) > 0) {
            fee.setStatus(Fee.FeeStatus.PARTIAL);
        } else if (fee.getDueDate() != null && fee.getDueDate().isBefore(LocalDate.now())) {
            fee.setStatus(Fee.FeeStatus.OVERDUE);
        } else {
            fee.setStatus(Fee.FeeStatus.PENDING);
        }
    }

    private FeeDto toDto(Fee f) {
        FeeDto dto = new FeeDto();
        dto.setId(f.getId());
        dto.setStudentId(f.getStudent().getId());
        dto.setStudentName(f.getStudent().getUser().getFirstName() + " " + f.getStudent().getUser().getLastName());
        dto.setRollNumber(f.getStudent().getRollNumber());
        dto.setFeeType(f.getFeeType());
        dto.setAmount(f.getAmount());
        dto.setAmountPaid(f.getAmountPaid() == null ? BigDecimal.ZERO : f.getAmountPaid());
        dto.setBalance(f.getAmount().subtract(f.getAmountPaid() == null ? BigDecimal.ZERO : f.getAmountPaid()));
        dto.setDueDate(f.getDueDate());
        dto.setPaidDate(f.getPaidDate());
        dto.setStatus(f.getStatus().name());
        dto.setSemester(f.getSemester());
        return dto;
    }
}