package com.github.not.n0w.lifepilot.aiEngine.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class AiResponse {
    private final String answerToUser;
    private String advice;
    private String analysis;
    private final JsonNode toolCalls;

    public AiResponse(String answerToUser, String advice, String analysis) {
        this.answerToUser = answerToUser;
        this.advice = advice;
        this.analysis = analysis;
        this.toolCalls = null;
    }

}