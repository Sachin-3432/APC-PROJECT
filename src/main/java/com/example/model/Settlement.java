package com.example.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "settlements")
public class Settlement {
    @Id
    private String id;
    
    private String payerId;
    private String receiverId;
    private Double amount;
    private String paymentMethod;
    private String description;
    private LocalDateTime createdAt = LocalDateTime.now();
}
