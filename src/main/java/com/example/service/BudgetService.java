package com.example.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.example.dto.BudgetRequest;
import com.example.dto.BudgetResponse;
import com.example.model.Budget;
import com.example.model.Expense;
import com.example.model.User;
import com.example.repository.BudgetRepository;
import com.example.repository.ExpenseRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserService userService;

    public BudgetResponse getUserBudget(String email) {
        try {
            User user = userService.findByEmail(email);
            String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

            // Try to find a budget for current month. If not found, do NOT auto-create a
            // default budget
            // to avoid persisting unintended defaults. Instead, return the most recently
            // set budget amount
            // when available (so user preferences carry over across months), but do not
            // save it automatically.
            java.util.Optional<Budget> optBudget = budgetRepository.findByUserIdAndMonth(user.getId(), currentMonth);

            Double totalExpenses = 0.0;
            try {
                totalExpenses = expenseRepository.findByUserIdOrderByDateDesc(user.getId())
                        .stream()
                        .filter(expense -> expense.getDate() != null)
                        .filter(expense -> {
                            try {
                                return expense.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))
                                        .equals(currentMonth);
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .mapToDouble(Expense::getAmount)
                        .sum();
            } catch (Exception e) {
                // If expense query fails, keep totalExpenses as 0
                totalExpenses = 0.0;
            }

            if (optBudget.isPresent()) {
                Budget budget = optBudget.get();
                budget.setSpent(totalExpenses);
                budgetRepository.save(budget);

                BudgetResponse response = new BudgetResponse();
                response.setAmount(budget.getAmount());
                response.setSpent(budget.getSpent());
                response.setRemaining(budget.getAmount() - budget.getSpent());
                return response;
            } else {
                // No budget set for current month. Use last user-set budget amount if
                // available, else fallback to 10000.
                java.util.List<Budget> budgets = budgetRepository.findByUserIdOrderByMonthDesc(user.getId());
                Double lastAmount = 10000.0;
                if (budgets != null && !budgets.isEmpty()) {
                    lastAmount = budgets.get(0).getAmount();
                }
                BudgetResponse response = new BudgetResponse();
                response.setAmount(lastAmount);
                response.setSpent(totalExpenses);
                response.setRemaining(lastAmount - totalExpenses);
                return response;
            }
        } catch (Exception e) {
            // If something goes wrong, try to return the user's most recent budget instead
            // of blindly
            // returning the 10k default. This helps preserve user-set budgets despite
            // transient errors.
            try {
                User user = userService.findByEmail(email);
                java.util.List<Budget> budgets = budgetRepository.findByUserIdOrderByMonthDesc(user.getId());
                if (budgets != null && !budgets.isEmpty()) {
                    Budget last = budgets.get(0);
                    BudgetResponse response = new BudgetResponse();
                    response.setAmount(last.getAmount());
                    response.setSpent(last.getSpent() != null ? last.getSpent() : 0.0);
                    response.setRemaining(last.getAmount() - response.getSpent());
                    return response;
                }
            } catch (Exception ex) {
                // ignore and fall through to default
            }

            // Final fallback
            BudgetResponse response = new BudgetResponse();
            response.setAmount(10000.0);
            response.setSpent(0.0);
            response.setRemaining(10000.0);
            return response;
        }
    }

    public BudgetResponse setBudget(String email, BudgetRequest request) {
        try {
            User user = userService.findByEmail(email);
            String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

            // Always update the existing budget for this user/month, or insert if not
            // exists
            Budget budget = budgetRepository.findByUserIdAndMonth(user.getId(), currentMonth)
                    .orElse(null);

            if (budget == null) {
                budget = new Budget();
                budget.setUserId(user.getId());
                budget.setMonth(currentMonth);
                budget.setSpent(0.0);
            }
            budget.setAmount(request.getAmount());

            budget = budgetRepository.save(budget);

            BudgetResponse response = new BudgetResponse();
            response.setAmount(budget.getAmount());
            response.setSpent(budget.getSpent());
            response.setRemaining(budget.getAmount() - budget.getSpent());

            return response;
        } catch (Exception e) {
            // Return a default budget response on error
            BudgetResponse response = new BudgetResponse();
            response.setAmount(request != null ? request.getAmount() : 10000.0);
            response.setSpent(0.0);
            response.setRemaining(request != null ? request.getAmount() : 10000.0);
            return response;
        }
    }

}
