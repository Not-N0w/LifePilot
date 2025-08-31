package com.github.not.n0w.lifepilot.service;

import com.github.not.n0w.lifepilot.model.AssistantResponse;

import java.util.Map;

public interface AIService {
    public AssistantResponse sendMessage(Long userId, String message);
    void pushMessageToUser(Long userId, String message);
}
