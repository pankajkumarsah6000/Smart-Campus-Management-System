package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AIChatRequest {
    @NotBlank
    private String message;
    private String sessionId;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}
