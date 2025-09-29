package com.example.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "debts")
public class Debt {
    @Id
    private String id;
    
    private String debtorId;
    private String creditorId;
    private Double amount;
    private String expenseId;
    private Boolean settled = false;
}
