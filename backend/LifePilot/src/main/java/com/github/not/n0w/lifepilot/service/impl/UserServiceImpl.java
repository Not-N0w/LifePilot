package com.github.not.n0w.lifepilot.service.impl;

import com.github.not.n0w.lifepilot.model.AiTaskType;
import com.github.not.n0w.lifepilot.model.DialogStyle;
import com.github.not.n0w.lifepilot.model.Metric;
import com.github.not.n0w.lifepilot.model.User;
import com.github.not.n0w.lifepilot.repository.MetricRepository;
import com.github.not.n0w.lifepilot.repository.UserRepository;
import com.github.not.n0w.lifepilot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MetricRepository metricRepository;

    public User registerUser(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            log.error("Username '{}' already exists", username);
            throw new AuthenticationException("User with username '" + username + "' already exists.") {};
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setUsualDialogStyle(DialogStyle.BASE);
        user.setTask(AiTaskType.ACQUAINTANCE);
        user = userRepository.save(user);
        log.info("New user created: {}", user.toString());
        return user;
    }

    public Map<String, Object> getUserMetrics(String username) {
        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AuthenticationException("User not found") {}
        );
        List<Metric> metricList = metricRepository.findLatestMetricsByUserId(user.getId());
        Map<String, Integer> metricsMap = metricList.stream()
                .collect(Collectors.toMap(
                        m -> m.getMetricType().toString(),
                        Metric::getMetricValue
                ));

        response.put("metrics", metricsMap);
        log.info("User metrics: {}", metricList);

        return response;
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
}