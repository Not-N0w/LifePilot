package com.github.not.n0w.lifepilot.controller;

import com.github.not.n0w.lifepilot.model.User;
import com.github.not.n0w.lifepilot.repository.UserRepository;
import com.github.not.n0w.lifepilot.service.JwtService;
import com.github.not.n0w.lifepilot.service.MailService;
import com.github.not.n0w.lifepilot.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final MailService mailService;

    @Data
    public static class AuthRequest {
        private String username;
        private String password;
    }
    @Data
    public static class ConfirmEmailRequest {
        private int code;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody AuthRequest request) {
        log.info("Register request received");

        String username = request.getUsername();
        if (username == null || !username.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Invalid email format"
            ));
        }

        var user = userService.registerUser(username, request.getPassword());
        log.info("User registered successfully");

        return verifyUser(request);
    }


    @PostMapping("/verification")
    public ResponseEntity<Map<String, String>> verifyUser(@RequestBody AuthRequest request) {
        log.info("Verify user request received");

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new AuthenticationException("User not found") {});

        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        String verificationToken = jwtService.generateVerificationToken(request.getUsername(), code);
        userService.sendVerifyMail(request.getUsername(), code);

        log.info("Successfully sent verification token and mail");
        return ResponseEntity.ok(Map.of(
                "verificationToken", verificationToken
        ));
    }

    @PostMapping("/confirm-email")
    public ResponseEntity<Map<String, String>> confirmEmailByCode(
            @RequestHeader("Verification") String token,
            @RequestBody ConfirmEmailRequest request) {

        log.info("Confirm email request received");
        int confirmationCode = request.getCode();

        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "Token not provided"));
        }

        try {
            String email = jwtService.validateVerificationCode(token, confirmationCode);

            User user = userService.verifyUser(email);
            log.info("User email verified successfully: {}", email);

            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String accessToken = jwtService.generateToken(userDetails, user.getId(), user.getIsVerified());
            String refreshToken = jwtService.generateRefreshToken(userDetails, user.getId());

            return ResponseEntity.ok(Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken
            ));
        } catch (IllegalArgumentException ex) {
            log.warn("Confirmation failed: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Confirmation failed"
            ));
        }
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthRequest request) {
        log.info("Login request received");

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new AuthenticationException("User not found") {});

        if (!user.getIsVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "status", "error",
                            "message", "Email not verified"
                    ));
        }

        String accessToken = jwtService.generateToken(userDetails, user.getId(), true);
        String refreshToken = jwtService.generateRefreshToken(userDetails, user.getId());

        log.info("Successfully logged in");
        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }

    public record RefreshRequest(String refreshToken) {}

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody RefreshRequest request) {
        log.info("Refresh request received");

        String refreshToken = request.refreshToken;
        if (refreshToken == null || refreshToken.isBlank()) {
            log.info("No refresh token found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Refresh token is required"));
        }

        if (!jwtService.isTokenValid(refreshToken)) {
            log.info("Refresh token is invalid");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid refresh token"));
        }

        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("User not found") {});

        String newAccessToken = jwtService.generateToken(userDetails, user.getId(), user.getIsVerified());

        log.info("Successfully refreshed token");
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }


    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteAccount(HttpServletRequest request) {
        log.info("Delete account request received");

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authorization header is missing or invalid"));
        }

        String token = authHeader.substring(7);
        if (!jwtService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid token"));
        }

        String username = jwtService.extractUsername(token);
        userRepository.findByUsername(username).ifPresent(userRepository::delete);

        log.info("Successfully deleted account for user: {}", username);
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }



    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handle(AuthenticationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", ex.getMessage()));
    }
}

