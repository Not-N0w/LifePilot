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
import com.github.not.n0w.lifepilot.model.Metric;
import com.github.not.n0w.lifepilot.model.MetricType;
import com.github.not.n0w.lifepilot.repository.UserRepository;
import com.github.not.n0w.lifepilot.repository.MetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetMetricsTask implements AiTask {
    private final PromptLoader promptLoader;
    private final AiTextClient aiTextClient;
    private final UserRepository userRepository;
    private final ToolRegistry toolRegistry;
    private final MetricRepository metricRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AiTaskType getType() {
        return AiTaskType.GET_METRICS;
    }

    public List<Metric> extractMetricsFromJson(JsonNode argumentsRaw, Long userId) {
        List<Metric> metrics = new ArrayList<>();
        JsonNode arguments;
        String toolName;
        try {
            arguments = argumentsRaw.get(0).get("function").get("arguments");
            toolName = argumentsRaw.get(0).get("function").get("name").asText();
        }
        catch (Exception e) {
            log.error("Wrong format of tool call");
            return metrics;
        }
        if(!Objects.equals(toolName, "set_metrics")) {
            log.error("Wrong tool call");
            return metrics;
        }
        try {
            if (arguments.isNull()) {
                return metrics;
            }

            if (arguments.isTextual()) {
                arguments = objectMapper.readTree(arguments.asText());
            }

            if (!arguments.isObject()) {
                return metrics;
            }

            ObjectNode objectNode = (ObjectNode) arguments;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String metricName = entry.getKey();
                JsonNode valueNode = entry.getValue();

                if (valueNode != null && valueNode.isInt()) {
                    try {
                        MetricType metricType = MetricType.fromKey(metricName);

                        Metric metric = new Metric();
                        metric.setMetricType(metricType);
                        metric.setMetricValue(valueNode.asInt());
                        metric.setUserId(userId);

                        metrics.add(metric);
                    } catch (IllegalArgumentException e) {
                        log.warn("Unknown metric type: {}", metricName);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Failed to extract metrics from json: {}", arguments.asText());
        }

        return metrics;
    }

    @Override
    public UserSession execute(UserSession userSession, User user) {
        log.info("Executing GetMetricsTask");
        AiResponse response = null;

        String analyzePrompt = promptLoader.loadPromptText("taskPrompts/getMetrics/GetMetricsAnalyzePrompt.txt");
        UserSession analyzeMetricsUserSession = new UserSession(
                List.of(new Message("system", analyzePrompt)),
                userSession.getUserMessages()
        );
        response = aiTextClient.ask(analyzeMetricsUserSession, List.of(toolRegistry.getSetMetricsToolCall()));

        boolean isNoToolCalls = response == null || response.getToolCalls().isEmpty();
        if(!isNoToolCalls) {
            List<Metric> previousMetrics = metricRepository.findLatestMetricsByUserId(user.getId());
            String metricsStr = "Предыдущие метрики пользователя: " +
                    (previousMetrics == null ? "" : previousMetrics.toString()) + '\n';
            userSession.addSystemMessage(metricsStr);

            List<Metric> metrics = extractMetricsFromJson(response.getToolCalls(), user.getId());
            log.info("New metrics found: {}", metrics.toString());

            metricRepository.saveAll(metrics);
            log.info("Metrics saved to database");
        }
        return userSession;
    }
}
