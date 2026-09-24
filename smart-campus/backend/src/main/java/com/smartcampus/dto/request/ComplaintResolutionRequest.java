package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ComplaintResolutionRequest {
    @NotBlank(message = "Status is required")
    private String status; // IN_PROGRESS, RESOLVED, CLOSED

    private String resolutionNote;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }
}