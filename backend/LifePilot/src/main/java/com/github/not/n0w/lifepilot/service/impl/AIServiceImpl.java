package com.github.not.n0w.lifepilot.service.impl;

import com.github.not.n0w.lifepilot.aiEngine.AiManager;
import com.github.not.n0w.lifepilot.aiEngine.model.AiRequest;
import com.github.not.n0w.lifepilot.aiEngine.model.AiResponse;
import com.github.not.n0w.lifepilot.aiEngine.model.Message;
import com.github.not.n0w.lifepilot.model.*;
import com.github.not.n0w.lifepilot.repository.UserRepository;
import com.github.not.n0w.lifepilot.repository.SavedMessagesRepository;
import com.github.not.n0w.lifepilot.service.AIService;
import com.github.not.n0w.lifepilot.service.InitInteractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {
    private final AiManager aiManager;
    private final UserRepository userRepository;
    private final SavedMessagesRepository savedMessagesRepository;
    private final InitInteractionService initInteractionService;

    @Override
    public void pushMessageToUser(Long userId, String message) {
        initInteractionService.pushMessage(userId, message);
    }

    @Override
    public AssistantResponse sendMessage(Long userId, String userText) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        List<SavedMessage> savedMessages = savedMessagesRepository.findAllByUserId(userId);
        List<Message> messageList = new java.util.ArrayList<>(savedMessages
                .stream()
                .map(msg -> new Message(msg.getRole(), msg.getMessage()))
                .toList());
        messageList.add(new Message("user", userText));

        SavedMessage userMessage = new SavedMessage();
        userMessage.setUser(user);
        userMessage.setMessage(userText);
        userMessage.setRole("user");
        savedMessagesRepository.save(userMessage);

        AiRequest request = new AiRequest(user, messageList);

        AiResponse response = aiManager.process(request);
        log.info("Response from gpt: {}", response.toString());

        SavedMessage responseMessage = new SavedMessage();
        responseMessage.setUser(user);
        responseMessage.setMessage(
                response.getAnswerToUser() +
                "<advice>" + response.getAdvice() + "</advice>" +
                "<analysis>" + response.getAnalysis() + "/<analysis>"
        );
        responseMessage.setRole("assistant");
        savedMessagesRepository.save(responseMessage);

        return new AssistantResponse(response.getAnswerToUser(), response.getAdvice(), response.getAnalysis());

    }
}
