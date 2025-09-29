package com.example.repository;

import com.example.model.Debt;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DebtRepository extends MongoRepository<Debt, String> {
    List<Debt> findByDebtorIdAndSettledFalse(String debtorId);
    List<Debt> findByCreditorIdAndSettledFalse(String creditorId);
}
