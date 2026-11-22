package com.smartcampus.dto.response;

import java.util.List;

public class StudentAttendanceSummaryDto {
    private double overallPercentage;
    private List<SubjectAttendance> bySubject;
    private List<AttendanceRecordDto> recentRecords;

    public static class SubjectAttendance {
        private String subjectName;
        private long present;
        private long total;
        private double percentage;

        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
        public long getPresent() { return present; }
        public void setPresent(long present) { this.present = present; }
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }

    public double getOverallPercentage() { return overallPercentage; }
    public void setOverallPercentage(double overallPercentage) { this.overallPercentage = overallPercentage; }
    public List<SubjectAttendance> getBySubject() { return bySubject; }
    public void setBySubject(List<SubjectAttendance> bySubject) { this.bySubject = bySubject; }
    public List<AttendanceRecordDto> getRecentRecords() { return recentRecords; }
    public void setRecentRecords(List<AttendanceRecordDto> recentRecords) { this.recentRecords = recentRecords; }
}
