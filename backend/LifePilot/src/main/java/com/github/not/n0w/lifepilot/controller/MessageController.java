package com.github.not.n0w.lifepilot.controller;


import com.github.not.n0w.lifepilot.model.User;
import com.github.not.n0w.lifepilot.repository.UserRepository;
import com.github.not.n0w.lifepilot.service.JwtService;
import com.github.not.n0w.lifepilot.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;
    private final UserRepository userRepository;

    public record TextRequest(String text) {}

    @PostMapping(value = "/text-message", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public Map<String, String> handleTextMessage(@RequestBody TextRequest request) {
        String text = request.text();

        String username = SecurityContextHolder.getContext().getAuthentication().getName();


        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AuthenticationException("User not found") {}
        );


        String gptResponse = messageService.handleTextMessage(user.getId(), text);

        log.info("Response sent successfully");
        return Map.of(
                "role", "assistant",
                "text", gptResponse
        );
    }

    @PostMapping(value = "/audio-message", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public Map<String, String> handleAudioMessage(
            @RequestPart("audio") MultipartFile audio
    ) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();


        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AuthenticationException("User not found") {}
        );

        String gptResponse = messageService.handleAudioMessage(user.getId(), audio);
        log.info("Response sent successfully");
        return Map.of("text", gptResponse);

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
