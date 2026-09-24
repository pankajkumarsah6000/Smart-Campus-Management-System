package com.smartcampus.service;

import com.smartcampus.dto.request.MarkAttendanceRequest;
import com.smartcampus.dto.response.AttendanceRecordDto;
import com.smartcampus.dto.response.StudentAttendanceSummaryDto;
import java.util.List;

public interface AttendanceService {
    List<AttendanceRecordDto> markAttendance(MarkAttendanceRequest request, Long markedByFacultyUserId, boolean admin);
    List<AttendanceRecordDto> getAttendanceForSubjectAndDate(Long subjectId, java.time.LocalDate date);
    StudentAttendanceSummaryDto getSummaryForStudent(Long studentId);
    StudentAttendanceSummaryDto getSummaryForStudentByUserId(Long studentUserId);

    /**
     * Summary for a specific student, optionally enforcing that the requesting
     * parent is linked to that student. Pass null parentUserId when the caller
     * is ADMIN/FACULTY.
     */
    StudentAttendanceSummaryDto getSummaryForStudentChecked(Long studentId, Long parentUserId);
}
