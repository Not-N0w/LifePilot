package com.github.not.n0w.lifepilot.controller;


import com.github.not.n0w.lifepilot.model.User;
import com.github.not.n0w.lifepilot.repository.UserRepository;
import com.github.not.n0w.lifepilot.service.JwtService;
import com.github.not.n0w.lifepilot.service.MessageService;
import com.github.not.n0w.lifepilot.service.job.Job;
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
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;
    private final UserRepository userRepository;

    public record TextRequest(String text) {}

    @PostMapping(value = "/text", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<Map<String, String>> handleTextMessage(@RequestBody TextRequest request) {
        String text = request.text();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AuthenticationException("User not found") {}
        );


        String jobId = messageService.handleTextMessage(user.getId(), text);

        log.info("Job id sent successfully");
        return ResponseEntity.ok(Map.of(
                "job_id", jobId
        ));
    }

    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<Map<String, String>> handleAudioMessage(
            @RequestPart("audio") MultipartFile audio
    ) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AuthenticationException("User not found") {}
        );

        String jobId = messageService.handleAudioMessage(user.getId(), audio);
        log.info("Job id sent successfully");
        return ResponseEntity.ok(Map.of(
                "job_id", jobId
        ));

    }

    @GetMapping(value = "/checkout", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<Map<String, String>> checkoutGptResponse(@RequestParam("job_id") String jobId) {
        log.info("Checkout job with id = {}", jobId);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AuthenticationException("User not found") {}
        );

        Optional<Job> job = messageService.jobCheckout(user.getId(), jobId);
        if(job.isEmpty()) {
            log.info("No such job");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Job not found"));
        }

        var jobResponse = job.get();
        log.info("Job found: {}", jobResponse);
        if(jobResponse.getStatus().equals(Job.JobStatus.PENDING)) {
            return ResponseEntity.ok(Map.of(
                    "job_id", jobId,
                    "status", "PENDING"
            ));
        }
        else {
            return ResponseEntity.ok(Map.of(
                    "job_id", jobId,
                    "status", "DONE",
                    "role", "assistant",
                    "message", jobResponse.getGptResponse()
                    ));
        }
    }


    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handle(AuthenticationException ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "message", ex.getMessage())
                );
    }
}
