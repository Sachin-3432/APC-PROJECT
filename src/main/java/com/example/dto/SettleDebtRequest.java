package com.example.dto;

import lombok.Data;

@Data
public class SettleDebtRequest {
    private String creditorId;
    private Double amount;
    private String paymentMethod;
    private String description;
}
