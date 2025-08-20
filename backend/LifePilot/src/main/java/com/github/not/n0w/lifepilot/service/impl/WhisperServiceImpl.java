package com.github.not.n0w.lifepilot.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.not.n0w.lifepilot.service.WhisperService;
import com.jayway.jsonpath.internal.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.nio.file.Files;


@Service
@Slf4j
@RequiredArgsConstructor
public class WhisperServiceImpl implements WhisperService {

    @Qualifier("whisperClient")
    @Autowired
    private WebClient whisperWebClient;

    @Override
    public String voiceToText(MultipartFile voiceFile) {
        try {
            long maxInMemorySize = 10 * 1024 * 1024;
            Object resource;

            if (voiceFile.getSize() <= maxInMemorySize) {
                byte[] bytes = voiceFile.getBytes();
                resource = new ByteArrayResource(bytes) {
                    @Override
                    public String getFilename() {
                        return voiceFile.getOriginalFilename() != null
                                ? voiceFile.getOriginalFilename()
                                : "voice.ogg";
                    }
                };
            } else {
                Path tmp = (Path) Files.createTempFile("voice-", ".ogg");
                voiceFile.transferTo((File) tmp);
                resource = new FileSystemResource(((java.nio.file.Path) tmp).toFile());
            }

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", resource)
                    .filename(voiceFile.getOriginalFilename() != null
                            ? voiceFile.getOriginalFilename()
                            : "voice.ogg")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM);

            String response = whisperWebClient.post()
                    .uri("/transcribe")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Whisper API response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Failed to send voice file to Whisper", e);
            throw new RuntimeException("Whisper transcription failed", e);
        }
    }


}
