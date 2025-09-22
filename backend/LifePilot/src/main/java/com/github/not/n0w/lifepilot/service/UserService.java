package com.github.not.n0w.lifepilot.service;

import com.github.not.n0w.lifepilot.model.User;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface UserService {
    public User registerUser(String username, String password) throws AuthenticationException;
    public Map<String, String> getUserInfo(String username);
    public Map<String, Object> getUserMetrics(String username);
    public Map<String, Object> getUserLbsPoints(String username);
    public void sendVerifyMail(String username, int code);
    public User verifyUser(String username);
}