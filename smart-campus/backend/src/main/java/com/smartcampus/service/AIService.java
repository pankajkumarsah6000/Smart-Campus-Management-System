package com.smartcampus.service;

import com.smartcampus.dto.request.AIChatRequest;
import com.smartcampus.dto.response.AIChatResponse;

public interface AIService {
    AIChatResponse chat(AIChatRequest request, Long userId);
}
