package com.example.dto;

import lombok.Data;

@Data
public class UserResponse {
    private String id;
    private String fullName;
    private String email;
    private String profileImage;
    private java.util.List<String> roles;
    private boolean active;
}
