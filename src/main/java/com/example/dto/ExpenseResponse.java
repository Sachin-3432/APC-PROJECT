package com.example.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ExpenseResponse {
    private String id;
    private String description;
    private Double amount;
    private String category;
    private LocalDate date;
    private String groupName;
    private String paidByName;
}
