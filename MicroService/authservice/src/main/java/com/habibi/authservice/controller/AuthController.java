package com.habibi.authservice.controller;

import com.habibi.authservice.dto.*;
import com.habibi.authservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody RegisterRequest request) {
        return Map.of("message", authService.register(request));
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        String token = authService.login(request);
        return Map.of("token", token);
    }
}