package com.smartcampus.service;

import com.smartcampus.dto.response.*;

import java.util.List;

public interface ParentService {
    ParentProfileDto getMyProfile(Long userId);
    List<StudentDto> getMyChildren(Long userId);
    StudentAttendanceSummaryDto getChildAttendance(Long parentUserId, Long studentId);
    StudentDashboardDto getChildDashboard(Long parentUserId, Long studentId);
    List<ResultDto> getChildResults(Long parentUserId, Long studentId);
    List<FeeDto> getChildFees(Long parentUserId, Long studentId);
    List<TimetableSlotDto> getChildTimetable(Long parentUserId, Long studentId);
}