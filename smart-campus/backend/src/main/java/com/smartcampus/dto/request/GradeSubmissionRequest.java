package com.smartcampus.dto.request;

public class GradeSubmissionRequest {
    private Double marksAwarded;
    private String feedback;

    public Double getMarksAwarded() { return marksAwarded; }
    public void setMarksAwarded(Double marksAwarded) { this.marksAwarded = marksAwarded; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}