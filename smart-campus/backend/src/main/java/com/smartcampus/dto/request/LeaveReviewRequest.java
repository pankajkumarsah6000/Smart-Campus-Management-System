package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotBlank;

public class LeaveReviewRequest {
    @NotBlank
    private String status;
    private String comment;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
