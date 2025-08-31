package com.github.not.n0w.lifepilot.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class AssistantResponse {
    private final String answer;
    private final String advice;
    private final String analysis;
}
