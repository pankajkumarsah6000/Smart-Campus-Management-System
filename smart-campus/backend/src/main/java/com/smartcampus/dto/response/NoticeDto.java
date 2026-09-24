package com.smartcampus.dto.response;

import java.time.LocalDateTime;

public class NoticeDto {
    private Long id;
    private String title;
    private String content;
    private String postedByName;
    private String targetAudience;
    private String priority;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getPostedByName() { return postedByName; }
    public void setPostedByName(String postedByName) { this.postedByName = postedByName; }
    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
