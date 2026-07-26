package com.habibi.authservice.service;

import com.habibi.authservice.dto.*;
import com.habibi.authservice.model.*;
import com.habibi.authservice.repository.UserRepository;
import com.habibi.authservice.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public String register(RegisterRequest request) {

        User user = new User();
        user.setUsername(request.username);
        user.setEmail(request.email);
        user.setPassword(request.password); // nanti bisa hash
        user.setFullName(request.fullName);
        user.setRole(Role.USER);

        userRepository.save(user);

        return "Register berhasil";
    }

    public String login(LoginRequest request) {

        User user = userRepository.findByUsername(request.username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        if (!user.getPassword().equals(request.password)) {
            throw new RuntimeException("Password salah");
        }

        return JwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }
}