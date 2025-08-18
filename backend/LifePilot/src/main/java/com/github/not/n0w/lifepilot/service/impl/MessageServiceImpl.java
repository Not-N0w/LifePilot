package com.github.not.n0w.lifepilot.service.impl;

import com.github.not.n0w.lifepilot.service.AIService;
import com.github.not.n0w.lifepilot.service.MessageService;
import com.github.not.n0w.lifepilot.service.WhisperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final AIService aiService;
    private final WhisperService whisperService;

    @Override
    public String handleTextMessage(Long userId, String text) {
        String response;

        if(text != null && !text.isEmpty()) {
            response = aiService.sendMessage(userId, text);
        }
        else {
            log.error("Received text message is empty");
            throw new RuntimeException("Received text message is empty");
        }
        return response;
    }

    @Override
    public String handleAudioMessage(Long userId, MultipartFile audioFile) {

        String response;
        if(audioFile != null) {
            log.info("Loaded audio file to whisper");
            String textFromVoice = whisperService.voiceToText(audioFile);
            log.info("Audio transcribed successfully");

            response = aiService.sendMessage(userId, textFromVoice);
        }
        else {
            log.error("No audio message received");
            throw new RuntimeException("No audio message received");
        }

        return response;
    }
}
