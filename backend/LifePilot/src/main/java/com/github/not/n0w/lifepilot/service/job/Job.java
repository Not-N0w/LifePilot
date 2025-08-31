package com.github.not.n0w.lifepilot.service.job;

import com.github.not.n0w.lifepilot.model.AssistantResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.Timestamp;
import java.time.Instant;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class Job {
    private final String id;
    private final Long userId;
    private final String userRequest;
    private final Instant expires;
    private JobStatus status;
    private AssistantResponse assistantResponse;

    public static enum JobStatus {
        PENDING, DONE
    }
}
