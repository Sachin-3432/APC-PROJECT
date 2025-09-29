package com.example.dto;

import lombok.Data;

@Data
public class BudgetResponse {
    private Double amount;
    private Double spent;
    private Double remaining;
}
