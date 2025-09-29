package com.example.controller;

import com.example.dto.*;
import com.example.service.DebtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;

@RestController
@RequestMapping("/api/debts")
@CrossOrigin(origins = "*")
public class DebtController {

    @Autowired
    private DebtService debtService;

    @GetMapping
    public ResponseEntity<List<DebtResponse>> getDebts(Authentication authentication) {
        try {
            String email = authentication.getName();
            List<DebtResponse> debts = debtService.getUserDebts(email);
            return ResponseEntity.ok(debts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/settle")
    public ResponseEntity<Void> settleDebt(
            Authentication authentication,
            @RequestBody SettleDebtRequest request) {
        try {
            String email = authentication.getName();
            debtService.settleDebt(email, request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
