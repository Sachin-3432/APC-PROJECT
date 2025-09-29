package com.example.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;

@Data
public class ExpenseRequest {
    private String description;
    private Double amount;
    private String category;
    private LocalDate date;
    private String groupId;
    private String paidBy;
    private List<String> participantIds;
    private Map<String, Double> shares;
}
