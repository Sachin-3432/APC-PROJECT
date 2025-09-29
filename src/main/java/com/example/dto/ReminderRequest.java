package com.example.dto;

import lombok.Data;

@Data
public class ReminderRequest {
    private String debtorId;
    private String message;
    private String method;
}
