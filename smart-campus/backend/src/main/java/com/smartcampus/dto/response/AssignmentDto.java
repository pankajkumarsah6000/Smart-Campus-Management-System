package com.smartcampus.dto.response;

import java.time.LocalDateTime;

public class AssignmentDto {
    private Long id;
    private String title;
    private String description;
    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    private Long createdByFacultyId;
    private String createdByFacultyName;
    private LocalDateTime dueDate;
    private Integer maxMarks;
    private String attachmentUrl;
    // Student view
    private Boolean submitted;
    private String submissionStatus;
    private Double awardedMarks;
    // Faculty view
    private Long submissionCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public Long getCreatedByFacultyId() { return createdByFacultyId; }
    public void setCreatedByFacultyId(Long createdByFacultyId) { this.createdByFacultyId = createdByFacultyId; }
    public String getCreatedByFacultyName() { return createdByFacultyName; }
    public void setCreatedByFacultyName(String createdByFacultyName) { this.createdByFacultyName = createdByFacultyName; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public Integer getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    public Boolean getSubmitted() { return submitted; }
    public void setSubmitted(Boolean submitted) { this.submitted = submitted; }
    public String getSubmissionStatus() { return submissionStatus; }
    public void setSubmissionStatus(String submissionStatus) { this.submissionStatus = submissionStatus; }
    public Double getAwardedMarks() { return awardedMarks; }
    public void setAwardedMarks(Double awardedMarks) { this.awardedMarks = awardedMarks; }
    public Long getSubmissionCount() { return submissionCount; }
    public void setSubmissionCount(Long submissionCount) { this.submissionCount = submissionCount; }
}