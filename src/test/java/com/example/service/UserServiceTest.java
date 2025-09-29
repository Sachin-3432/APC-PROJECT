package com.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
// ...existing code...
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.dto.LoginRequest;
// ...existing code...
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.util.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class UserServiceTest {
    private UserRepository userRepository;
    private JwtUtil jwtUtil;
    private UserService userService;

    @BeforeEach
    void setup() {
        userRepository = Mockito.mock(UserRepository.class);
        jwtUtil = Mockito.mock(JwtUtil.class);
        userService = new UserService();
        org.springframework.test.util.ReflectionTestUtils.setField(userService, "userRepository", userRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(userService, "passwordEncoder",
                new BCryptPasswordEncoder());
        org.springframework.test.util.ReflectionTestUtils.setField(userService, "jwtUtil", jwtUtil);
    }

    @Test
    void loginReturnsTokenIfPasswordMatches() {
        LoginRequest req = new LoginRequest();
        req.setEmail("login@example.com");
        req.setPassword("pw");
        User user = new User();
        user.setEmail("login@example.com");
        user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("pw"));
        when(userRepository.findByEmail("login@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("login@example.com")).thenReturn("tok");
        var resp = userService.login(req);
        assertEquals("tok", resp.getToken());
    }
}
