package com.github.not.n0w.lifepilot.service;

import com.github.not.n0w.lifepilot.model.AssistantResponse;

public interface AIService {
    public AssistantResponse sendMessage(Long userId, String message);
    void pushMessageToUser(Long userId, String message);
}
