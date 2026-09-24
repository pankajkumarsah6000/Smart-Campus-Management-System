package com.smartcampus.dto.response;

public class AdminDashboardDto {
    private long totalStudents;
    private long totalFaculty;
    private long totalDepartments;
    private double overallAttendancePercentage;
    private long pendingLeaveRequests;
    private double feeCollectionPercentage;

    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }
    public long getTotalFaculty() { return totalFaculty; }
    public void setTotalFaculty(long totalFaculty) { this.totalFaculty = totalFaculty; }
    public long getTotalDepartments() { return totalDepartments; }
    public void setTotalDepartments(long totalDepartments) { this.totalDepartments = totalDepartments; }
    public double getOverallAttendancePercentage() { return overallAttendancePercentage; }
    public void setOverallAttendancePercentage(double overallAttendancePercentage) { this.overallAttendancePercentage = overallAttendancePercentage; }
    public long getPendingLeaveRequests() { return pendingLeaveRequests; }
    public void setPendingLeaveRequests(long pendingLeaveRequests) { this.pendingLeaveRequests = pendingLeaveRequests; }
    public double getFeeCollectionPercentage() { return feeCollectionPercentage; }
    public void setFeeCollectionPercentage(double feeCollectionPercentage) { this.feeCollectionPercentage = feeCollectionPercentage; }
}
