package com.example.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;

@Data
@Document(collection = "expenses")
public class Expense {
    @Id
    private String id;
    
    private String description;
    private Double amount;
    private String category;
    private LocalDate date;
    private String userId;
    private String groupId;
    private String paidBy;
    private List<String> participantIds;
    private Map<String, Double> shares;
}
