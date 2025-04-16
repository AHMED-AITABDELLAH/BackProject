package com.savorybites.backend.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_IN_HOURS = 1;
    private static class AttemptInfo {
        int attempts;
        LocalDateTime firstAttemptTime;
    }
    private final Map<String, AttemptInfo> attemptsCache = new ConcurrentHashMap<>();



    public boolean isBlocked(String clientIP) {

        AttemptInfo info = attemptsCache.get(clientIP);
        if (info == null) return false;

        if (info.attempts >= MAX_ATTEMPTS) {
            LocalDateTime unblockTime = info.firstAttemptTime.plusHours(BLOCK_DURATION_IN_HOURS);
            if (LocalDateTime.now().isBefore(unblockTime)) {
                return true;
            } else {
                attemptsCache.remove(clientIP); // Reset after time expires
            }
        }
        return false;
    }

    public void loginFailed(String email) {
        AttemptInfo info = attemptsCache.getOrDefault(email, new AttemptInfo());
        if (info.attempts == 0) {
            info.firstAttemptTime = LocalDateTime.now();
        }
        info.attempts++;
        attemptsCache.put(email, info);
    }

    public void loginSucceeded(String email) {
        attemptsCache.remove(email); // reset on successful login
    }
}