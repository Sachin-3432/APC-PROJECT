// ...existing code...
package com.example.service;

import java.util.List;
import java.util.stream.Collectors;

import com.example.dto.AuthResponse;
import com.example.dto.LoginRequest;
import com.example.dto.RegisterRequest;
import com.example.dto.UserResponse;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public List<UserResponse> getAllNonAdminUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user != null && user.getRoles() != null && !user.getRoles().contains("ROLE_ADMIN"))
                .map(user -> {
                    UserResponse response = new UserResponse();
                    response.setId(user.getId());
                    response.setFullName(user.getFullName());
                    response.setEmail(user.getEmail());
                    response.setRoles(user.getRoles());
                    response.setProfileImage(user.getProfileImage());
                    response.setActive(user.isActive());
                    return response;
                })
                .collect(Collectors.toList());
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public void blockUser(String userId) {
        User user = findById(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    public void unblockUser(String userId) {
        User user = findById(userId);
        user.setActive(true);
        userRepository.save(user);
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new com.example.exception.BadRequestException("Account with this email already exists");
        }
        // Validate full name: only alphabets and spaces
        if (request.getFullName() == null || !request.getFullName().matches("[A-Za-z ]+")) {
            throw new com.example.exception.BadRequestException("Full name can only contain letters and spaces");
        }
        User user = new User();
        user.setId(java.util.UUID.randomUUID().toString());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRoles(java.util.List.of("ROLE_USER"));
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getFullName(), user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new com.example.exception.BadRequestException("Invalid credentials"));

        if (!user.isActive()) {
            throw new com.example.exception.BadRequestException("Account is blocked. Contact admin.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new com.example.exception.BadRequestException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getFullName(), user.getEmail());
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new com.example.exception.ResourceNotFoundException("User not found"));

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setProfileImage(user.getProfileImage());
        response.setRoles(user.getRoles());
        return response;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new com.example.exception.ResourceNotFoundException("User not found"));
    }

    public User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new com.example.exception.ResourceNotFoundException("User not found"));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    UserResponse response = new UserResponse();
                    response.setId(user.getId());
                    response.setFullName(user.getFullName());
                    response.setEmail(user.getEmail());
                    response.setRoles(user.getRoles());
                    response.setProfileImage(user.getProfileImage());
                    response.setActive(user.isActive());
                    return response;
                })
                .collect(Collectors.toList());
    }

    // Role management helpers
    public void addRoleToUser(String userId, String role) {
        User user = findById(userId);
        if (user.getRoles() == null)
            user.setRoles(new java.util.ArrayList<>());
        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            userRepository.save(user);
        }
    }

    public void removeRoleFromUser(String userId, String role) {
        User user = findById(userId);
        if (user.getRoles() != null && user.getRoles().remove(role)) {
            userRepository.save(user);
        }
    }

    public UserResponse updateProfile(String email, UserResponse updateRequest) {
        User user = findByEmail(email);
        if (updateRequest.getFullName() != null) {
            if (!updateRequest.getFullName().matches("[A-Za-z ]+")) {
                throw new com.example.exception.BadRequestException("Full name can only contain letters and spaces");
            }
            user.setFullName(updateRequest.getFullName());
        }
        if (updateRequest.getEmail() != null) {
            user.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getProfileImage() != null) {
            user.setProfileImage(updateRequest.getProfileImage());
        }
        userRepository.save(user);
        return getCurrentUser(user.getEmail());
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
