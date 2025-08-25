package com.example.app.taskmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTasksService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasksService.class);

    private final LoginService loginService;

    public ScheduledTasksService(LoginService loginService) {
        this.loginService = loginService;
    }

    /**
     * Clean up expired password reset tokens every hour
     */
    @Scheduled(fixedRate = 3600000) // Run every hour (3600000 ms)
    public void cleanupExpiredTokens() {
        logger.debug("Starting cleanup of expired password reset tokens");
        try {
            loginService.cleanupExpiredTokens();
        } catch (Exception e) {
            logger.error("Error during token cleanup", e);
        }
    }

    /**
     * Health check task - runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes (300000 ms)
    public void healthCheck() {
        logger.debug("Application health check completed");
        // Add any health check logic here
    }
}