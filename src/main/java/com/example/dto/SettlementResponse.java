package com.example.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SettlementResponse {
    private String id;
    private String payerId;
    private String payerName;
    private String receiverId;
    private String receiverName;
    private Double amount;
    private String paymentMethod;
    private String description;
    private LocalDateTime createdAt;
}
