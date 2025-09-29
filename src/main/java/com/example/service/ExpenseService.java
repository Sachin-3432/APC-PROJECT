package com.example.service;

import java.util.List;
import java.util.stream.Collectors;

import com.example.dto.ExpenseRequest;
import com.example.dto.ExpenseResponse;
import com.example.model.Expense;
import com.example.model.Group;
import com.example.model.User;
import com.example.repository.ExpenseRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private DebtService debtService;

    public List<ExpenseResponse> getUserExpenses(String email) {
        User user = userService.findByEmail(email);
        List<Expense> expenses = expenseRepository.findByUserIdOrderByDateDesc(user.getId());

        return expenses.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public ExpenseResponse createExpense(String email, ExpenseRequest request) {
        User user = userService.findByEmail(email);

        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        // Ensure date is always set as LocalDate, even if frontend sends a string
        if (request.getDate() == null && request instanceof java.util.Map) {
            Object dateObj = ((java.util.Map<?, ?>) request).get("date");
            if (dateObj instanceof String) {
                try {
                    expense.setDate(java.time.LocalDate.parse((String) dateObj));
                } catch (Exception ex) {
                    expense.setDate(null);
                }
            } else {
                expense.setDate(null);
            }
        } else {
            expense.setDate(request.getDate());
        }
        expense.setUserId(user.getId());
        expense.setGroupId(request.getGroupId());
        expense.setPaidBy(request.getPaidBy());
        expense.setParticipantIds(request.getParticipantIds());
        expense.setShares(request.getShares());

        expense = expenseRepository.save(expense);

        if (request.getParticipantIds() != null && !request.getParticipantIds().isEmpty()) {
            debtService.createDebtsFromExpense(expense);
        }

        return convertToResponse(expense);
    }

    public ExpenseResponse updateExpense(String id, ExpenseRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new com.example.exception.ResourceNotFoundException("Expense not found"));

        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate());

        expense = expenseRepository.save(expense);
        return convertToResponse(expense);
    }

    public void deleteExpense(String id) {
        expenseRepository.deleteById(id);
    }

    public ExpenseResponse getExpenseById(String id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new com.example.exception.ResourceNotFoundException("Expense not found"));
        return convertToResponse(expense);
    }

    private ExpenseResponse convertToResponse(Expense expense) {
        ExpenseResponse response = new ExpenseResponse();
        response.setId(expense.getId());
        response.setDescription(expense.getDescription());
        response.setAmount(expense.getAmount());
        response.setCategory(expense.getCategory());
        response.setDate(expense.getDate());

        if (expense.getGroupId() != null) {
            try {
                Group group = groupService.findById(expense.getGroupId());
                response.setGroupName(group.getName());
            } catch (Exception e) {
                response.setGroupName("Unknown Group");
            }
        }

        if (expense.getPaidBy() != null) {
            try {
                User paidByUser = userService.findById(expense.getPaidBy());
                response.setPaidByName(paidByUser.getFullName());
            } catch (Exception e) {
                response.setPaidByName("Unknown User");
            }
        }

        return response;
    }
}
