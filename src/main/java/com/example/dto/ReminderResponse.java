package com.example.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReminderResponse {
    private String id;
    private String senderName;
    private String message;
    private Boolean read;
    private LocalDateTime createdAt;
}
