package com.example.dto;

import lombok.Data;

@Data
public class DebtResponse {
    private String creditorId;
    private String creditorName;
    private Double amount;
}
