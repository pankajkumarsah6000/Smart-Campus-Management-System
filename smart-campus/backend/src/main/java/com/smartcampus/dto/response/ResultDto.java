package com.smartcampus.dto.response;

public class ResultDto {
    private Long id;
    private Long examinationId;
    private String examinationName;
    private Long subjectId;
    private String subjectName;
    private Long studentId;
    private String studentName;
    private String rollNumber;
    private Double marksObtained;
    private Integer maxMarks;
    private String grade;
    private Boolean passed;
    private String remarks;
    private Double percentage;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getExaminationId() { return examinationId; }
    public void setExaminationId(Long examinationId) { this.examinationId = examinationId; }
    public String getExaminationName() { return examinationName; }
    public void setExaminationName(String examinationName) { this.examinationName = examinationName; }
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public Double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Double marksObtained) { this.marksObtained = marksObtained; }
    public Integer getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
}