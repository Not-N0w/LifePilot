package com.github.not.n0w.lifepilot.service.impl;

import com.github.not.n0w.lifepilot.model.*;
import com.github.not.n0w.lifepilot.repository.MetricRepository;
import com.github.not.n0w.lifepilot.repository.TaskRepository;
import com.github.not.n0w.lifepilot.repository.UserRepository;
import com.github.not.n0w.lifepilot.service.MailService;
import com.github.not.n0w.lifepilot.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MetricRepository metricRepository;
    private final TaskRepository taskRepository;
    private final MailService mailService;


    public User registerUser(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            log.error("Username '{}' already exists", username);
            throw new AuthenticationException("User with username '" + username + "' already exists.") {};
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setUsualDialogStyle(DialogStyle.BASE);
        user.setTasks(taskRepository.findAll());
        user.setIsVerified(false);
        user = userRepository.save(user);

        List<Metric> metrics = List.of(
                new Metric(user.getId(), MetricType.MENTAL_STATE, 50),
                new Metric(user.getId(), MetricType.PHYSICAL_STATE, 50),
                new Metric(user.getId(), MetricType.SOCIAL_ENVIRONMENT, 50),
                new Metric(user.getId(), MetricType.GOALS_AND_ACTIONS, 50)
        );
        metricRepository.saveAll(metrics);

        log.info("New user created: {}", user.toString());
        return user;
    }

    public Map<String, Object> getUserMetrics(String username) {
        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AuthenticationException("User not found") {}
        );
        List<Metric> metricList = metricRepository.findLatestMetricsByUserId(user.getId());
        List<Metric> previousMetrics = metricRepository.findPreviousMetricsByUserId(user.getId());
        Integer currentGeneral = (int)Math.round(
                metricList.stream()
                        .mapToInt(Metric::getMetricValue)
                        .average()
                        .orElse(0.0));
        Integer previousGeneral = (int)Math.round(
                previousMetrics.stream()
                        .mapToInt(Metric::getMetricValue)
                        .average()
                        .orElse(0.0));

        @AllArgsConstructor
        @Data
        class MetricComparison {
            private String metricType;
            private Integer currentValue;
            private Integer previousValue;
        }
        List<MetricComparison> comparisons = new ArrayList<>(Arrays.stream(MetricType.values())
                .map(type -> {
                    Integer current = metricList.stream()
                            .filter(m -> m.getMetricType() == type)
                            .map(Metric::getMetricValue)
                            .findFirst()
                            .orElse(-1);

                    Integer previous = previousMetrics.stream()
                            .filter(m -> m.getMetricType() == type)
                            .map(Metric::getMetricValue)
                            .findFirst()
                            .orElse(-1);

                    return new MetricComparison(type.getKey(), current, previous);
                })
                .toList());

        comparisons.add(
                new MetricComparison(
                        "general",
                        currentGeneral,
                        previousGeneral
                )
        );

        response.put("metrics", comparisons);
        log.info("User metrics: {}", metricList);

        return response;
    }

    private Integer getLbsPoint(List<Metric> metrics) {
        return (int) Math.round(
                metrics.stream()
                        .collect(Collectors.groupingBy(metric -> metric.getMetricType().toString(),
                                Collectors.collectingAndThen(
                                        Collectors.maxBy(Comparator.comparing(Metric::getCreatedAt)),
                                        optionalMetric -> optionalMetric.map(Metric::getMetricValue).orElse(null)
                                )
                        ))
                        .values()
                        .stream()
                        .filter(Objects::nonNull)
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(0.0)
        );
    }
    private boolean isClose(LocalDateTime a, LocalDateTime b, int seconds) {
        return Math.abs(java.time.Duration.between(a, b).getSeconds()) <= seconds;
    }

    @Override
    public Map<String, Object> getUserLbsPoints(String username) {
        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AuthenticationException("User not found") {}
        );

        List<Metric> metricList = metricRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId());
        List<Integer> lbsPoints = new ArrayList<>();

        LocalDateTime createdAtSaved = LocalDateTime.MIN;
        for(int i = 0; i < metricList.size(); i++) {
            if (isClose(createdAtSaved, metricList.get(i).getCreatedAt(), 30)) {
                continue;
            }
            createdAtSaved = metricList.get(i).getCreatedAt();
            lbsPoints.add(getLbsPoint(metricList.subList(i, metricList.size())));
        }
        response.put("points", lbsPoints);
        log.info("User lbs points: {}", lbsPoints);
        return response;
    }
    @Override
    public void sendVerifyMail(String username, int code) {
        String subject = "Подтверждение вашей почты";

        String text = String.format(
                "Здравствуйте, %s!\n\n" +
                        "Ваш код подтверждения: %06d\n" +
                        "Срок действия кода: 10 минут.\n\n" +
                        "Если вы не регистрировались, просто проигнорируйте это письмо.",
                username,
                code
        );

        mailService.sendMail(username, subject, text);
    }

    @Override
    public Map<String, String> getUserInfo(String username) {
        Map<String, String> response = new HashMap<>();

        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AuthenticationException("User not found") {}
        );

        response.put("username", user.getUsername());
        response.put("name", user.getName());
        response.put("gender", user.getGender());
        response.put("dialog_style", String.valueOf(user.getUsualDialogStyle()));
        log.info("User info: {}", response);

        return response;
    }

    @Override
    public User verifyUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AuthenticationException("User not found") {}
        );
        user.setIsVerified(true);
        userRepository.save(user);
        return user;
    }
}