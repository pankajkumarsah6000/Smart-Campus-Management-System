package com.smartcampus.service.impl;

import com.smartcampus.dto.response.*;
import com.smartcampus.entity.Parent;
import com.smartcampus.entity.Student;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.UnauthorizedException;
import com.smartcampus.repository.ParentRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParentServiceImpl implements ParentService {

    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;
    private final AttendanceService attendanceService;
    private final ExaminationService examinationService;
    private final FeeService feeService;
    private final TimetableService timetableService;
    private final StudentService studentService;

    @Override
    public ParentProfileDto getMyProfile(Long userId) {
        Parent parent = findParentByUserId(userId);
        ParentProfileDto dto = new ParentProfileDto();
        dto.setId(parent.getId());
        dto.setUserId(parent.getUser().getId());
        dto.setEmail(parent.getUser().getEmail());
        dto.setFirstName(parent.getUser().getFirstName());
        dto.setLastName(parent.getUser().getLastName());
        dto.setPhone(parent.getUser().getPhone());
        dto.setRelationship(parent.getRelationship());
        if (parent.getChildren() != null) {
            dto.setChildrenCount(parent.getChildren().size());
        }
        return dto;
    }

    @Override
    public List<StudentDto> getMyChildren(Long userId) {
        Parent parent = findParentByUserId(userId);
        return parent.getChildren().stream().map(this::toStudentDto).collect(Collectors.toList());
    }

    @Override
    public StudentAttendanceSummaryDto getChildAttendance(Long parentUserId, Long studentId) {
        return attendanceService.getSummaryForStudentChecked(studentId, parentUserId);
    }

    @Override
    public StudentDashboardDto getChildDashboard(Long parentUserId, Long studentId) {
        verifyChildLink(parentUserId, studentId);
        Student student = findStudentById(studentId);
        return studentService.getDashboard(student.getUser().getId());
    }

    @Override
    public List<ResultDto> getChildResults(Long parentUserId, Long studentId) {
        verifyChildLink(parentUserId, studentId);
        return examinationService.getResultsForStudentById(studentId);
    }

    @Override
    public List<FeeDto> getChildFees(Long parentUserId, Long studentId) {
        return feeService.getFeesForParentChild(parentUserId, studentId);
    }

    @Override
    public List<TimetableSlotDto> getChildTimetable(Long parentUserId, Long studentId) {
        verifyChildLink(parentUserId, studentId);
        return timetableService.getTimetableForStudent(studentId);
    }

    private Parent findParentByUserId(Long userId) {
        return parentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No parent profile linked to this account"));
    }

    private Student findStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
    }

    private void verifyChildLink(Long parentUserId, Long studentId) {
        Parent parent = findParentByUserId(parentUserId);
        boolean linked = parent.getChildren() != null
                && parent.getChildren().stream().anyMatch(c -> c.getId().equals(studentId));
        if (!linked) {
            throw new UnauthorizedException("You can only access data for your linked children");
        }
    }

    private StudentDto toStudentDto(Student s) {
        StudentDto dto = new StudentDto();
        dto.setId(s.getId());
        dto.setUserId(s.getUser().getId());
        dto.setEmail(s.getUser().getEmail());
        dto.setFirstName(s.getUser().getFirstName());
        dto.setLastName(s.getUser().getLastName());
        dto.setPhone(s.getUser().getPhone());
        dto.setRollNumber(s.getRollNumber());
        if (s.getDepartment() != null) {
            dto.setDepartmentId(s.getDepartment().getId());
            dto.setDepartmentName(s.getDepartment().getName());
        }
        dto.setSemester(s.getSemester());
        dto.setSection(s.getSection());
        dto.setStatus(s.getStatus().name());
        return dto;
    }
}