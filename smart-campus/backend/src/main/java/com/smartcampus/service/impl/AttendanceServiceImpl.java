package com.smartcampus.service.impl;

import com.smartcampus.dto.request.MarkAttendanceRequest;
import com.smartcampus.dto.response.AttendanceRecordDto;
import com.smartcampus.dto.response.StudentAttendanceSummaryDto;
import com.smartcampus.entity.*;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.*;
import com.smartcampus.service.AttendanceService;
import com.smartcampus.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final FacultyRepository facultyRepository;
    private final ParentRepository parentRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public List<AttendanceRecordDto> markAttendance(MarkAttendanceRequest request, Long markedByFacultyUserId, boolean admin) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));

        final Faculty faculty;
        if (admin) {
            faculty = null;
        } else {
            faculty = facultyRepository.findByUserId(markedByFacultyUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("No faculty profile linked to this account"));
            // A faculty member may only mark attendance for subjects they are assigned to.
            if (subject.getFaculty() == null || !subject.getFaculty().getId().equals(faculty.getId())) {
                throw new com.smartcampus.exception.UnauthorizedException(
                        "You are not assigned to teach this subject");
            }
        }

        List<AttendanceRecordDto> results = request.getEntries().stream().map(entry -> {
            Student student = studentRepository.findById(entry.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + entry.getStudentId()));

            Attendance attendance = attendanceRepository
                    .findByStudentIdAndSubjectId(student.getId(), subject.getId()).stream()
                    .filter(a -> a.getAttendanceDate().equals(request.getAttendanceDate()))
                    .findFirst()
                    .orElse(new Attendance());

            attendance.setStudent(student);
            attendance.setSubject(subject);
            attendance.setMarkedBy(faculty);
            attendance.setAttendanceDate(request.getAttendanceDate());
            attendance.setStatus(Attendance.AttendanceStatus.valueOf(entry.getStatus()));
            attendance.setRemarks(entry.getRemarks());

            attendance = attendanceRepository.save(attendance);

            if (attendance.getStatus() == Attendance.AttendanceStatus.ABSENT) {
                notificationService.notifyUser(
                        student.getUser().getId(),
                        "Marked absent — " + subject.getName(),
                        "You were marked absent for " + subject.getName() + " on " + request.getAttendanceDate() + ".",
                        "ATTENDANCE",
                        "/student/attendance"
                );
            }

            return toDto(attendance);
        }).collect(Collectors.toList());

        return results;
    }

    @Override
    public List<AttendanceRecordDto> getAttendanceForSubjectAndDate(Long subjectId, LocalDate date) {
        return attendanceRepository.findBySubjectIdAndAttendanceDate(subjectId, date).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public StudentAttendanceSummaryDto getSummaryForStudent(Long studentId) {
        List<Attendance> records = attendanceRepository.findByStudentId(studentId);
        return buildSummary(records);
    }

    @Override
    public StudentAttendanceSummaryDto getSummaryForStudentByUserId(Long studentUserId) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No student profile linked to this account"));
        return getSummaryForStudent(student.getId());
    }

    @Override
    public StudentAttendanceSummaryDto getSummaryForStudentChecked(Long studentId, Long parentUserId) {
        if (parentUserId != null) {
            Parent parent = parentRepository.findByUserId(parentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("No parent profile linked to this account"));
            boolean linked = parent.getChildren().stream()
                    .anyMatch(c -> c.getId().equals(studentId));
            if (!linked) {
                throw new com.smartcampus.exception.UnauthorizedException(
                        "You can only view attendance for your linked children");
            }
        }
        return getSummaryForStudent(studentId);
    }

    private StudentAttendanceSummaryDto buildSummary(List<Attendance> records) {
        StudentAttendanceSummaryDto summary = new StudentAttendanceSummaryDto();

        long total = records.size();
        long present = records.stream().filter(a -> a.getStatus() == Attendance.AttendanceStatus.PRESENT).count();
        summary.setOverallPercentage(total == 0 ? 0.0 : (present * 100.0) / total);

        Map<String, List<Attendance>> bySubject = records.stream()
                .collect(Collectors.groupingBy(a -> a.getSubject().getName()));

        summary.setBySubject(bySubject.entrySet().stream().map(e -> {
            StudentAttendanceSummaryDto.SubjectAttendance sa = new StudentAttendanceSummaryDto.SubjectAttendance();
            sa.setSubjectName(e.getKey());
            long subjTotal = e.getValue().size();
            long subjPresent = e.getValue().stream().filter(a -> a.getStatus() == Attendance.AttendanceStatus.PRESENT).count();
            sa.setTotal(subjTotal);
            sa.setPresent(subjPresent);
            sa.setPercentage(subjTotal == 0 ? 0.0 : (subjPresent * 100.0) / subjTotal);
            return sa;
        }).collect(Collectors.toList()));

        summary.setRecentRecords(records.stream()
                .sorted((a, b) -> b.getAttendanceDate().compareTo(a.getAttendanceDate()))
                .limit(20)
                .map(this::toDto)
                .collect(Collectors.toList()));

        return summary;
    }

    private AttendanceRecordDto toDto(Attendance a) {
        AttendanceRecordDto dto = new AttendanceRecordDto();
        dto.setId(a.getId());
        dto.setStudentId(a.getStudent().getId());
        dto.setStudentName(a.getStudent().getUser().getFirstName() + " " + a.getStudent().getUser().getLastName());
        dto.setRollNumber(a.getStudent().getRollNumber());
        dto.setSubjectName(a.getSubject().getName());
        dto.setAttendanceDate(a.getAttendanceDate());
        dto.setStatus(a.getStatus().name());
        dto.setRemarks(a.getRemarks());
        return dto;
    }
}
