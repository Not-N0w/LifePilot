package com.github.not.n0w.lifepilot.service.impl;

import com.github.not.n0w.lifepilot.service.AIService;
import com.github.not.n0w.lifepilot.service.MessageService;
import com.github.not.n0w.lifepilot.service.WhisperService;
import com.github.not.n0w.lifepilot.service.job.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final AIService aiService;
    private final WhisperService whisperService;
    private final Map<String, Job> jobs = new ConcurrentHashMap<>();


    private String startJob(Long userId, String text) {
        String jobId = UUID.randomUUID().toString();
        jobs.put(jobId,
                new Job(
                    jobId,
                    userId,
                    text,
                    Instant.now().plus(Duration.ofMinutes(20)),
                    Job.JobStatus.PENDING,
                    null
                )
        );
        log.info("Starting job {}", jobId);
        new Thread(() -> {
            Job job = new Job(
                    jobId,
                    userId,
                    text,
                    Instant.now().plus(Duration.ofMinutes(10)),
                    Job.JobStatus.DONE,
                    aiService.sendMessage(userId, text)
            );

            jobs.put(jobId, job);
        }).start();

        return jobId;
    }

    @Override
    public String handleTextMessage(Long userId, String text) {
        if(text == null || text.isEmpty()) {
            log.error("Received text message is empty");
            throw new RuntimeException("Received text message is empty");
        }

        return startJob(userId, text);
    }

    @Override
    public String handleAudioMessage(Long userId, MultipartFile audioFile) {
        if(audioFile == null || audioFile.isEmpty()) {
            log.error("No audio message received");
            throw new RuntimeException("No audio message received");
        }

        log.info("Loaded audio file to whisper");
        String textFromVoice = whisperService.voiceToText(audioFile);
        log.info("Audio transcribed successfully");

        return startJob(userId, textFromVoice);

    }

    @Override
    public Optional<Job> jobCheckout(Long userId, String jobId) {
        Instant now = Instant.now();
        jobs.entrySet().removeIf(entry -> entry.getValue().getExpires().isBefore(now));

        Job job = jobs.get(jobId);
        if(job == null) {
            return Optional.empty();
        }
        if(job.getUserId().equals(userId)) {
            Optional<Job> jobFound = Optional.of(job);
            jobs.remove(jobId);

            return jobFound;
        }

        throw new AuthenticationException("Job does not belong to this user") {};
    }
}
