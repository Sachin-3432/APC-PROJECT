package com.example.repository;

import com.example.model.Reminder;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ReminderRepository extends MongoRepository<Reminder, String> {
    List<Reminder> findByReceiverIdOrderByCreatedAtDesc(String receiverId);
}
