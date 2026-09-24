package com.smartcampus.dto.response;

import java.time.LocalDateTime;

public class AssignmentSummaryDto {
    private Long id;
    private String title;
    private String subjectName;
    private LocalDateTime dueDate;
    private boolean submitted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public boolean isSubmitted() { return submitted; }
    public void setSubmitted(boolean submitted) { this.submitted = submitted; }
}
