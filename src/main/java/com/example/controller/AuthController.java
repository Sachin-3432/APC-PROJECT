package com.example.controller;

import java.util.Base64;

import com.example.dto.AuthResponse;
import com.example.dto.LoginRequest;
import com.example.dto.RegisterRequest;
import com.example.dto.UserResponse;
import com.example.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @org.springframework.web.bind.annotation.RequestBody @jakarta.validation.Valid RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @org.springframework.web.bind.annotation.RequestBody @jakarta.validation.Valid LoginRequest request) {
        AuthResponse response = userService.login(request);
        // Set JWT as HttpOnly cookie so browser requests (GET /admin.html) get
        // authenticated server-side
        jakarta.servlet.http.Cookie jwtCookie = new jakarta.servlet.http.Cookie("JWT", response.getToken());
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge((int) (86400)); // 1 day
        // Recommended: set secure=true in production when using HTTPS

        return ResponseEntity.ok()
                .header("Set-Cookie",
                        String.format("JWT=%s; HttpOnly; Path=/; Max-Age=%d; SameSite=Lax", response.getToken(), 86400))
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Clear the JWT cookie by setting Max-Age=0
        return ResponseEntity.ok()
                .header("Set-Cookie", "JWT=; HttpOnly; Path=/; Max-Age=0; SameSite=Lax")
                .body(java.util.Collections.singletonMap("message", "Logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        try {
            String email = authentication.getName();
            UserResponse response = userService.getCurrentUser(email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            Authentication authentication,
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {
        try {
            String userEmail = authentication.getName();
            String profileImageBase64 = null;

            if (profileImage != null && !profileImage.isEmpty()) {
                profileImageBase64 = "data:" + profileImage.getContentType() + ";base64," +
                        Base64.getEncoder().encodeToString(profileImage.getBytes());
            }

            UserResponse updateRequest = new UserResponse();
            updateRequest.setFullName(fullName);
            updateRequest.setEmail(email);
            updateRequest.setProfileImage(profileImageBase64);

            UserResponse response = userService.updateProfile(userEmail, updateRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        try {
            boolean exists = userService.existsByEmail(email);
            return ResponseEntity.ok(java.util.Collections.singletonMap("exists", exists));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new com.example.dto.ErrorResponse("Server error"));
        }
    }
}
