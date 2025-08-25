package com.github.not.n0w.lifepilot.aiEngine.task.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.not.n0w.lifepilot.aiEngine.AiTextClient;
import com.github.not.n0w.lifepilot.aiEngine.model.AiResponse;
import com.github.not.n0w.lifepilot.aiEngine.model.UserSession;
import com.github.not.n0w.lifepilot.aiEngine.model.Message;
import com.github.not.n0w.lifepilot.aiEngine.prompt.PromptLoader;
import com.github.not.n0w.lifepilot.aiEngine.task.AiTask;
import com.github.not.n0w.lifepilot.aiEngine.tool.ToolRegistry;
import com.github.not.n0w.lifepilot.model.AiTaskType;
import com.github.not.n0w.lifepilot.model.User;
import com.github.not.n0w.lifepilot.repository.UserRepository;
import com.github.not.n0w.lifepilot.repository.MetricRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class AcquaintanceTask implements AiTask {
    private final PromptLoader promptLoader;
    private final AiTextClient aiTextClient;
    private final UserRepository userRepository;
    private final ToolRegistry toolRegistry;
    private final MetricRepository metricRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AiTaskType getType() {
        return AiTaskType.ACQUAINTANCE;
    }

    @Data
    static
    class UserInfo {
        private String name;
        private String gender;
    }

    private UserInfo extractUserInfoFromJson(JsonNode argumentsRaw) {

        UserInfo userInfo = new UserInfo();
        JsonNode arguments;
        String toolName;
        try {
            arguments = argumentsRaw.get(0).get("function").get("arguments");
            toolName = argumentsRaw.get(0).get("function").get("name").asText();
        }
        catch (Exception e) {
            log.error("Wrong format of tool call");
            return userInfo;
        }
        if(!Objects.equals(toolName, "set_user_info")) {
            log.error("Wrong tool call");
            return userInfo;
        }
        try {
            if (arguments.isNull()) {
                return userInfo;
            }

            if (arguments.isTextual()) {
                arguments = objectMapper.readTree(arguments.asText());
            }

            if (!arguments.isObject()) {
                return userInfo;
            }

            ObjectNode objectNode = (ObjectNode) arguments;

            userInfo.setName(objectNode.get("name").asText());
            userInfo.setGender(objectNode.get("gender").asText());

        } catch (Exception e) {
            log.error("Failed to extract metrics from json: {}", arguments.asText());
        }

        return userInfo;
    }


    @Override
    public UserSession execute(UserSession userSession, User user) {
        log.info("Executing acquaintance task");
        String analyzePrompt = promptLoader.loadPromptText("taskPrompts/acquaintance/AcquaintanceAnalyzePrompt.txt");
        UserSession analyzeMetricsUserSession = new UserSession(
                List.of(new Message("system", analyzePrompt)),
                userSession.getUserMessages()
        );
        AiResponse response = aiTextClient.ask(analyzeMetricsUserSession, List.of(toolRegistry.getSetUserInfoToolCall()));

        if(response.getToolCalls().isEmpty()) {
            log.info("No tool calls found -> continue acquaintance");
            String prompt = promptLoader.loadPromptText("taskPrompts/acquaintance/AcquaintancePrompt.txt");
            userSession.addSystemMessage(prompt);
        }
        else {
            user.removeTask(AiTaskType.ACQUAINTANCE); // !!!
            UserInfo userInfo = extractUserInfoFromJson(response.getToolCalls());
            log.info("Acquaintance ended. User info {}", userInfo.toString());

            user.setName(userInfo.getName());
            user.setGender(userInfo.getGender());
            userRepository.save(user);

        }
        return userSession;
    }
}
