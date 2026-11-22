package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotBlank;

public class NoticeRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    private String targetAudience; // ALL, STUDENTS, FACULTY, PARENTS
    private Long departmentId;
    private String priority; // LOW, NORMAL, HIGH, URGENT
    private java.time.LocalDateTime expiresAt;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public java.time.LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(java.time.LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
