package com.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
// ...existing code...
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.model.Expense;
import com.example.repository.ExpenseRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ExpenseServiceTest {
    private ExpenseRepository expenseRepository;
    private ExpenseService expenseService;

    private UserService userService;
    private GroupService groupService;
    private DebtService debtService;

    @BeforeEach
    void setup() {
        expenseRepository = Mockito.mock(ExpenseRepository.class);
        userService = Mockito.mock(UserService.class);
        groupService = Mockito.mock(GroupService.class);
        debtService = Mockito.mock(DebtService.class);
        expenseService = new ExpenseService();
        org.springframework.test.util.ReflectionTestUtils.setField(expenseService, "expenseRepository",
                expenseRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(expenseService, "userService", userService);
        org.springframework.test.util.ReflectionTestUtils.setField(expenseService, "groupService", groupService);
        org.springframework.test.util.ReflectionTestUtils.setField(expenseService, "debtService", debtService);
    }

    @Test
    void deleteExpenseCallsRepository() {
        doNothing().when(expenseRepository).deleteById("eid");
        expenseService.deleteExpense("eid");
        verify(expenseRepository).deleteById("eid");
    }

    @Test
    void getExpenseByIdReturnsResponse() {
        Expense exp = new Expense();
        exp.setId("eid");
        exp.setDescription("desc");
        exp.setAmount(10.0);
        exp.setCategory("cat");
        exp.setDate(java.time.LocalDate.now());
        when(expenseRepository.findById("eid")).thenReturn(java.util.Optional.of(exp));
        var resp = expenseService.getExpenseById("eid");
        assertNotNull(resp);
        assertEquals("desc", resp.getDescription());
    }
}
