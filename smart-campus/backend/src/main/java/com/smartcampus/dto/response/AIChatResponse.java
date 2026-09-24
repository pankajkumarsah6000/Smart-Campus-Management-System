package com.smartcampus.dto.response;

public class AIChatResponse {
    private String reply;
    private String intent;
    private String sessionId;

    public AIChatResponse() {}
    public AIChatResponse(String reply, String intent, String sessionId) {
        this.reply = reply;
        this.intent = intent;
        this.sessionId = sessionId;
    }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}
