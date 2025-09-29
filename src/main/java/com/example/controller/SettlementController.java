package com.example.controller;

import java.util.List;

import com.example.dto.SettlementResponse;
import com.example.service.DebtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settlements")
@CrossOrigin(origins = "*")
public class SettlementController {

    @Autowired
    private DebtService debtService;

    @GetMapping
    public ResponseEntity<List<SettlementResponse>> getSettlements(Authentication authentication) {
        try {
            String email = authentication.getName();
            List<SettlementResponse> settlements = debtService.getUserSettlements(email);
            return ResponseEntity.ok(settlements);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
