package com.github.not.n0w.lifepilot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.not.n0w.lifepilot.exception.TokenExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        String message;
        if (authException instanceof TokenExpiredException) {
            message = "tokenExpired";
        } else {
            message = authException.getMessage() != null
                    ? authException.getMessage()
                    : "Unauthorized";
        }

        new ObjectMapper().writeValue(response.getOutputStream(),
                Map.of("message", message));
    }
}
