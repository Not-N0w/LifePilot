package com.github.not.n0w.lifepilot.controller;

import com.github.not.n0w.lifepilot.repository.MetricRepository;
import com.github.not.n0w.lifepilot.repository.UserRepository;
import com.github.not.n0w.lifepilot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public Map<String, String> getUserInfo() {
        log.info("User info request received");

        Map<String, String> response = userService.getUserInfo(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        log.info("User info sent successfully");

        return response;
    }

    @GetMapping(value = "/metrics", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public Map<String, Object> getUserMetrics() {
        log.info("User metrics request received");

        Map<String, Object> response = userService.getUserMetrics(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        log.info("User metrics sent successfully");

        return response;
    }

    @GetMapping(value = "/lbs-points", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public Map<String, Object> getUserLbsPoints() {
        log.info("User life balance score points request received");

        Map<String, Object> response = userService.getUserLbsPoints(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        log.info("User life balance score points sent successfully");

        return response;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handle(AuthenticationException ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "role", "assistant",
                        "message", ex.getMessage())
                );
    }
}