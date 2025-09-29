package com.example.service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.model.Expense;
import com.example.model.User;
import com.example.repository.ExpenseRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserService userService;

    public Map<String, Object> getReports(String email) {
        User user = userService.findByEmail(email);
        List<Expense> expenses = expenseRepository.findByUserIdOrderByDateDesc(user.getId());

        Map<String, Object> reports = new HashMap<>();
        reports.put("monthly", getMonthlyReport(expenses));
        reports.put("category", getCategoryReport(expenses));

        return reports;
    }

    public Map<String, Double> getCategoryReport(String email) {
        User user = userService.findByEmail(email);
        List<Expense> expenses = expenseRepository.findByUserIdOrderByDateDesc(user.getId());
        return getCategoryReport(expenses);
    }

    private Map<String, Double> getMonthlyReport(List<Expense> expenses) {
        Map<String, Double> monthlyData = new HashMap<>();
        for (Expense expense : expenses) {
            if (expense.getDate() != null) {
                String month = expense.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                monthlyData.put(month, monthlyData.getOrDefault(month, 0.0) + expense.getAmount());
            }
        }
        return monthlyData;
    }

    private Map<String, Double> getCategoryReport(List<Expense> expenses) {
        Map<String, Double> categoryData = new HashMap<>();
        for (Expense expense : expenses) {
            if (expense.getCategory() != null) {
                String category = expense.getCategory();
                categoryData.put(category, categoryData.getOrDefault(category, 0.0) + expense.getAmount());
            }
        }
        return categoryData;
    }
}
