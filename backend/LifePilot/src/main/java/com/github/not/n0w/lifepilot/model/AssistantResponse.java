package com.github.not.n0w.lifepilot.model;

import com.github.not.n0w.lifepilot.aiEngine.model.Message;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class AssistantResponse {
    private final Message answer;
    private final Message advice;
    private final Message analysis;
}
