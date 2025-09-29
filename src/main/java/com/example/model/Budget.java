package com.example.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "budgets")
public class Budget {
    @Id
    private String id;
    
    private String userId;
    private Double amount;
    private Double spent = 0.0;
    private String month;
}
