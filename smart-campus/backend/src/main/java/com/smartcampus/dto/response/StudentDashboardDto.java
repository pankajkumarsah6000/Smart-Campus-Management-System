package com.smartcampus.dto.response;

import java.util.List;

public class StudentDashboardDto {
    private StudentDto profile;
    private double attendancePercentage;
    private long pendingAssignments;
    private Double cgpa;
    private String nextExamInfo;
    private List<TimetableSlotDto> todaysTimetable;
    private List<NoticeDto> recentNotices;
    private List<AssignmentSummaryDto> upcomingAssignments;

    public StudentDto getProfile() { return profile; }
    public void setProfile(StudentDto profile) { this.profile = profile; }
    public double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(double attendancePercentage) { this.attendancePercentage = attendancePercentage; }
    public long getPendingAssignments() { return pendingAssignments; }
    public void setPendingAssignments(long pendingAssignments) { this.pendingAssignments = pendingAssignments; }
    public Double getCgpa() { return cgpa; }
    public void setCgpa(Double cgpa) { this.cgpa = cgpa; }
    public String getNextExamInfo() { return nextExamInfo; }
    public void setNextExamInfo(String nextExamInfo) { this.nextExamInfo = nextExamInfo; }
    public List<TimetableSlotDto> getTodaysTimetable() { return todaysTimetable; }
    public void setTodaysTimetable(List<TimetableSlotDto> todaysTimetable) { this.todaysTimetable = todaysTimetable; }
    public List<NoticeDto> getRecentNotices() { return recentNotices; }
    public void setRecentNotices(List<NoticeDto> recentNotices) { this.recentNotices = recentNotices; }
    public List<AssignmentSummaryDto> getUpcomingAssignments() { return upcomingAssignments; }
    public void setUpcomingAssignments(List<AssignmentSummaryDto> upcomingAssignments) { this.upcomingAssignments = upcomingAssignments; }
}
