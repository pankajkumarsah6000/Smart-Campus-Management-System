package com.smartcampus.dto.request;

public class AssignmentSubmitRequest {
    // textAnswer and fileUrl are both optional; at least one should be present.
    private String textAnswer;
    private String fileUrl;

    public String getTextAnswer() { return textAnswer; }
    public void setTextAnswer(String textAnswer) { this.textAnswer = textAnswer; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
}