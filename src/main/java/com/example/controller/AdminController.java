package com.example.controller;

import java.util.List;

import com.example.dto.UserResponse;
import com.example.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private UserService userService;

    // List only non-admin users for admin management
    @GetMapping("/users/non-admin")
    public ResponseEntity<List<UserResponse>> listNonAdminUsers() {
        return ResponseEntity.ok(userService.getAllNonAdminUsers());
    }

    // Block a user
    @PatchMapping("/users/{id}/block")
    public ResponseEntity<Void> blockUser(@PathVariable String id) {
        userService.blockUser(id);
        return ResponseEntity.ok().build();
    }

    // Unblock a user
    @PatchMapping("/users/{id}/unblock")
    public ResponseEntity<Void> unblockUser(@PathVariable String id) {
        userService.unblockUser(id);
        return ResponseEntity.ok().build();
    }

    // Delete a user
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/users/{id}/roles")
    public ResponseEntity<Void> addRole(@PathVariable String id, @RequestParam String role) {
        userService.addRoleToUser(id, role);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{id}/roles")
    public ResponseEntity<Void> removeRole(@PathVariable String id, @RequestParam String role) {
        userService.removeRoleFromUser(id, role);
        return ResponseEntity.ok().build();
    }
}
