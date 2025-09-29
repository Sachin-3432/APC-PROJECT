package com.example.controller;

import com.example.dto.BudgetRequest;
import com.example.dto.BudgetResponse;
import com.example.service.BudgetService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/budget")
@CrossOrigin(origins = "*")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @GetMapping
    public ResponseEntity<BudgetResponse> getBudget(Authentication authentication) {
        try {
            String email = authentication.getName();
            BudgetResponse response = budgetService.getUserBudget(email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return default budget on error
            BudgetResponse defaultResponse = new BudgetResponse();
            defaultResponse.setAmount(10000.0);
            defaultResponse.setSpent(0.0);
            defaultResponse.setRemaining(10000.0);
            return ResponseEntity.ok(defaultResponse);
        }
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> setBudget(
            Authentication authentication,
            @RequestBody BudgetRequest request) {
        try {
            String email = authentication.getName();
            BudgetResponse response = budgetService.setBudget(email, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return default budget on error
            BudgetResponse defaultResponse = new BudgetResponse();
            defaultResponse.setAmount(request != null ? request.getAmount() : 10000.0);
            defaultResponse.setSpent(0.0);
            defaultResponse.setRemaining(request != null ? request.getAmount() : 10000.0);
            return ResponseEntity.ok(defaultResponse);
        }
    }
}
