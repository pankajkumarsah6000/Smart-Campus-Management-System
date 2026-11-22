package com.smartcampus.dto.response;

import java.util.List;

public class FacultyDashboardDto {
    private FacultyDto profile;
    private long subjectCount;
    private long totalStudents;
    private long pendingLeaveRequests;
    private long openComplaints;
    private List<SubjectDto> subjects;
    private List<AssignmentDto> recentAssignments;

    public FacultyDto getProfile() { return profile; }
    public void setProfile(FacultyDto profile) { this.profile = profile; }
    public long getSubjectCount() { return subjectCount; }
    public void setSubjectCount(long subjectCount) { this.subjectCount = subjectCount; }
    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }
    public long getPendingLeaveRequests() { return pendingLeaveRequests; }
    public void setPendingLeaveRequests(long pendingLeaveRequests) { this.pendingLeaveRequests = pendingLeaveRequests; }
    public long getOpenComplaints() { return openComplaints; }
    public void setOpenComplaints(long openComplaints) { this.openComplaints = openComplaints; }
    public List<SubjectDto> getSubjects() { return subjects; }
    public void setSubjects(List<SubjectDto> subjects) { this.subjects = subjects; }
    public List<AssignmentDto> getRecentAssignments() { return recentAssignments; }
    public void setRecentAssignments(List<AssignmentDto> recentAssignments) { this.recentAssignments = recentAssignments; }
}