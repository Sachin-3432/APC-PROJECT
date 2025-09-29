package com.example.repository;

import java.util.List;
import java.util.Optional;

import com.example.model.Budget;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface BudgetRepository extends MongoRepository<Budget, String> {
    Optional<Budget> findByUserIdAndMonth(String userId, String month);

    List<Budget> findByUserIdOrderByMonthDesc(String userId);
}
