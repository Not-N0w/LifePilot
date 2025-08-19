package com.github.not.n0w.lifepilot.service;


import com.github.not.n0w.lifepilot.service.job.Job;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface MessageService {
    public String handleTextMessage(Long userId, String text);
    public String handleAudioMessage(Long userId, MultipartFile audioFile);
    public Optional<Job> jobCheckout(Long userId, String jobId);

}
