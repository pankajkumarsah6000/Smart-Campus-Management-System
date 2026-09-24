package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ExaminationRequest {
    @NotBlank(message = "Examination name is required")
    private String name;

    @NotNull(message = "Subject is required")
    private Long subjectId;

    private LocalDateTime examDate;

    private Integer durationMinutes;

    private Integer maxMarks;

    private String venue;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public LocalDateTime getExamDate() { return examDate; }
    public void setExamDate(LocalDateTime examDate) { this.examDate = examDate; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Integer getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
}