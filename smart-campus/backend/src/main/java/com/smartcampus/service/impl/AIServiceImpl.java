package com.smartcampus.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.dto.request.AIChatRequest;
import com.smartcampus.dto.response.AIChatResponse;
import com.smartcampus.dto.response.StudentDashboardDto;
import com.smartcampus.entity.AIConversation;
import com.smartcampus.entity.User;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.AIConversationRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.service.AIService;
import com.smartcampus.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

/**
 * AI Campus Assistant. Calls the LLM provider from the BACKEND ONLY —
 * app.ai.provider-api-key is never sent to the frontend. Every exchange is
 * grounded with the student's real dashboard data (attendance, timetable,
 * assignments, notices) pulled from the database, then logged to
 * AIConversation for auditability.
 */
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final AIConversationRepository aiConversationRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StudentService studentService;

    @Value("${app.ai.provider-api-key}")
    private String apiKey;

    @Value("${app.ai.provider-base-url}")
    private String baseUrl;

    @Value("${app.ai.model}")
    private String model;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public AIChatResponse chat(AIChatRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String intent = classifyIntent(request.getMessage());
        String context = buildContext(userId, intent);
        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();

        String reply;
        if (apiKey == null || apiKey.isBlank()) {
            // No provider key configured: fall back to a context-grounded canned
            // response so the feature still demonstrates real data retrieval
            // without a live LLM call. Set AI_PROVIDER_API_KEY to enable full replies.
            reply = fallbackReply(intent, context);
        } else {
            reply = callLlm(user, request.getMessage(), context);
        }

        AIConversation conversation = new AIConversation();
        conversation.setUser(user);
        conversation.setUserMessage(request.getMessage());
        conversation.setAiResponse(reply);
        conversation.setIntent(intent);
        conversation.setSessionId(sessionId);
        aiConversationRepository.save(conversation);

        return new AIChatResponse(reply, intent, sessionId);
    }

    private String classifyIntent(String message) {
        String m = message.toLowerCase();
        if (m.contains("timetable") || m.contains("schedule") || m.contains("class today")) return "TIMETABLE_QUERY";
        if (m.contains("attendance") || m.contains("present") || m.contains("absent")) return "ATTENDANCE_QUERY";
        if (m.contains("notice") || m.contains("announcement")) return "NOTICE_SEARCH";
        if (m.contains("assignment") || m.contains("homework")) return "ASSIGNMENT_HELP";
        if (m.contains("exam") || m.contains("test date")) return "EXAM_QUERY";
        if (m.contains("study plan") || m.contains("study schedule")) return "STUDY_PLAN";
        if (m.contains("performance") || m.contains("marks") || m.contains("grade") || m.contains("cgpa")) return "PERFORMANCE_SUMMARY";
        return "GENERAL";
    }

    /** Pulls real data from the student's own dashboard to ground the AI's answer. */
    private String buildContext(Long userId, String intent) {
        try {
            StudentDashboardDto dash = studentService.getDashboard(userId);
            StringBuilder ctx = new StringBuilder();
            ctx.append("Student: ").append(dash.getProfile().getFirstName()).append(" ").append(dash.getProfile().getLastName());
            ctx.append(" | Roll: ").append(dash.getProfile().getRollNumber());
            ctx.append(" | Attendance: ").append(String.format("%.1f", dash.getAttendancePercentage())).append("%");
            ctx.append(" | Pending assignments: ").append(dash.getPendingAssignments());
            if (dash.getCgpa() != null) ctx.append(" | CGPA: ").append(dash.getCgpa());
            if (dash.getTodaysTimetable() != null && !dash.getTodaysTimetable().isEmpty()) {
                ctx.append(" | Today's classes: ");
                dash.getTodaysTimetable().forEach(t ->
                        ctx.append(t.getSubjectName()).append(" (").append(t.getStartTime()).append("-").append(t.getEndTime()).append("); "));
            }
            if (dash.getRecentNotices() != null && !dash.getRecentNotices().isEmpty()) {
                ctx.append(" | Recent notices: ");
                dash.getRecentNotices().forEach(n -> ctx.append(n.getTitle()).append("; "));
            }
            return ctx.toString();
        } catch (Exception e) {
            // Non-student accounts (faculty/admin/parent) don't have a student dashboard.
            return "No student profile linked to this account.";
        }
    }

    private String fallbackReply(String intent, String context) {
        return switch (intent) {
            case "ATTENDANCE_QUERY" -> "Here's what I have on file — " + context +
                    ". (Connect a live AI provider key for conversational answers; this is a data-grounded fallback.)";
            case "TIMETABLE_QUERY" -> "Your schedule from the system — " + context;
            default -> "I can see your latest campus data: " + context +
                    ". Ask me about your timetable, attendance, assignments, or notices for specifics. " +
                    "(No AI_PROVIDER_API_KEY is configured, so this is a grounded fallback rather than a generated answer.)";
        };
    }

    private String callLlm(User user, String message, String context) {
        try {
            String systemPrompt = "You are the Smart Campus AI Assistant, helping " + user.getFirstName() +
                    " with campus-related questions. Use ONLY the following real data about this student when relevant; " +
                    "if the question needs data not present here, say you don't have that information yet. " +
                    "Be concise and helpful.\n\nSTUDENT DATA:\n" + context;

            String requestBody = objectMapper.writeValueAsString(new java.util.LinkedHashMap<String, Object>() {{
                put("model", model);
                put("max_tokens", 600);
                put("system", systemPrompt);
                put("messages", java.util.List.of(java.util.Map.of("role", "user", "content", message)));
            }});

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return "The AI assistant is temporarily unavailable (provider returned " + response.statusCode() + "). " +
                        "Here's what I can tell you from your records: " + context;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode contentArray = root.path("content");
            StringBuilder text = new StringBuilder();
            if (contentArray.isArray()) {
                for (JsonNode block : contentArray) {
                    if ("text".equals(block.path("type").asText())) {
                        text.append(block.path("text").asText());
                    }
                }
            }
            return text.length() > 0 ? text.toString() : fallbackReply(classifyIntent(message), context);

        } catch (Exception e) {
            return "The AI assistant hit an error reaching the provider. Here's what I can tell you from your records: " + context;
        }
    }
}
