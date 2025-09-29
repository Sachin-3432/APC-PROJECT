package com.example.controller;

import com.example.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getReports(Authentication authentication) {
        try {
            String email = authentication.getName();
            Map<String, Object> reports = reportService.getReports(email);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/category")
    public ResponseEntity<Map<String, Double>> getCategoryReport(Authentication authentication) {
        try {
            String email = authentication.getName();
            Map<String, Double> categoryReport = reportService.getCategoryReport(email);
            return ResponseEntity.ok(categoryReport);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
