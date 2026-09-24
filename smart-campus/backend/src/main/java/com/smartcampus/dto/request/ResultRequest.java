package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotNull;

public class ResultRequest {
    private Long examinationId;

    @NotNull(message = "Student is required")
    private Long studentId;

    private Double marksObtained;

    private String grade;

    private Boolean passed;

    private String remarks;

    public Long getExaminationId() { return examinationId; }
    public void setExaminationId(Long examinationId) { this.examinationId = examinationId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Double marksObtained) { this.marksObtained = marksObtained; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}