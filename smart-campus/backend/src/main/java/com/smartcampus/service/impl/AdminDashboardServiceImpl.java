package com.smartcampus.service.impl;

import com.smartcampus.dto.response.AdminDashboardDto;
import com.smartcampus.entity.Fee;
import com.smartcampus.entity.LeaveRequest;
import com.smartcampus.repository.*;
import com.smartcampus.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final FeeRepository feeRepository;

    @Override
    public AdminDashboardDto getDashboard() {
        AdminDashboardDto dto = new AdminDashboardDto();
        dto.setTotalStudents(studentRepository.count());
        dto.setTotalFaculty(facultyRepository.count());
        dto.setTotalDepartments(departmentRepository.count());

        long totalAttendance = attendanceRepository.findAll().size();
        long presentCount = attendanceRepository.findAll().stream()
                .filter(a -> a.getStatus() == com.smartcampus.entity.Attendance.AttendanceStatus.PRESENT)
                .count();
        dto.setOverallAttendancePercentage(totalAttendance == 0 ? 0.0 : (presentCount * 100.0) / totalAttendance);

        dto.setPendingLeaveRequests(leaveRequestRepository.findByStatus(LeaveRequest.LeaveStatus.PENDING).size());

        List<Fee> fees = feeRepository.findAll();
        BigDecimal totalDue = fees.stream().map(Fee::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaid = fees.stream().map(f -> f.getAmountPaid() == null ? BigDecimal.ZERO : f.getAmountPaid())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setFeeCollectionPercentage(totalDue.compareTo(BigDecimal.ZERO) == 0 ? 0.0
                : totalPaid.divide(totalDue, 4, java.math.RoundingMode.HALF_UP).doubleValue() * 100.0);

        return dto;
    }
}
