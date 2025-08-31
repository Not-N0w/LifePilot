package com.github.not.n0w.lifepilot.aiEngine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSession {
    private List<Message> systemMessages = new ArrayList<>();
    private List<Message> userMessages = new ArrayList<>();

    public void addUserMessage(Message message) {
        userMessages.add(message);
    }
    public void addSystemMessage(String message) {
        systemMessages.add(
                new Message("system", message)
        );
    }

}