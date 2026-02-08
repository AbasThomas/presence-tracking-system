package com.aptech.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST controller for application status and health checks.
 */
@RestController
public class StatusController {

    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        return Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "service", "presencesystem");
    }

    @GetMapping("/")
    public String index() {
        return "Presence System Backend is running. Access /health for status.";
    }
}
