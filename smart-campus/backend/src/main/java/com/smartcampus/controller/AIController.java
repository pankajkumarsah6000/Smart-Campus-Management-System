package com.smartcampus.controller;

import com.smartcampus.dto.ApiResponse;
import com.smartcampus.dto.request.AIChatRequest;
import com.smartcampus.dto.response.AIChatResponse;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.AIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * AI Campus Assistant. The provider API key lives only in application.yml /
 * AI_PROVIDER_API_KEY on the backend — it never reaches the client.
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AIChatResponse>> chat(@Valid @RequestBody AIChatRequest request,
                                                              @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(aiService.chat(request, principal.getId())));
    }
}
