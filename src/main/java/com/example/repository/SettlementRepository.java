package com.example.repository;

import com.example.model.Settlement;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SettlementRepository extends MongoRepository<Settlement, String> {
}
