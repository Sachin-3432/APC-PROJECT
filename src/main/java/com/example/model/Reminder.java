package com.example.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "reminders")
public class Reminder {
    @Id
    private String id;
    
    private String senderId;
    private String receiverId;
    private String message;
    private String method;
    private Boolean read = false;
    private LocalDateTime createdAt = LocalDateTime.now();
}
