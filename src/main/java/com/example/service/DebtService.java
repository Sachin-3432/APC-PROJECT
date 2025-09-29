package com.example.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.dto.DebtResponse;
import com.example.dto.SettleDebtRequest;
import com.example.dto.SettlementResponse;
import com.example.model.Debt;
import com.example.model.Expense;
import com.example.model.Settlement;
import com.example.model.User;
import com.example.repository.DebtRepository;
import com.example.repository.ReminderRepository;
import com.example.repository.SettlementRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DebtService {

    @Autowired
    private DebtRepository debtRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ReminderRepository reminderRepository;

    public List<SettlementResponse> getUserSettlements(String email) {
        User user = userService.findByEmail(email);
        String userId = user.getId();
        List<Settlement> settlements = settlementRepository.findAll();
        return settlements.stream()
                .filter(s -> s.getPayerId().equals(userId) || s.getReceiverId().equals(userId))
                .map(s -> {
                    SettlementResponse resp = new SettlementResponse();
                    resp.setId(s.getId());
                    resp.setPayerId(s.getPayerId());
                    try {
                        User payer = userService.findById(s.getPayerId());
                        resp.setPayerName(payer.getFullName());
                    } catch (Exception e) {
                        resp.setPayerName("Unknown User");
                    }
                    resp.setReceiverId(s.getReceiverId());
                    try {
                        User receiver = userService.findById(s.getReceiverId());
                        resp.setReceiverName(receiver.getFullName());
                    } catch (Exception e) {
                        resp.setReceiverName("Unknown User");
                    }
                    resp.setAmount(s.getAmount());
                    resp.setPaymentMethod(s.getPaymentMethod());
                    resp.setDescription(s.getDescription());
                    resp.setCreatedAt(s.getCreatedAt());
                    return resp;
                })
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public List<DebtResponse> getUserDebts(String email) {
        User user = userService.findByEmail(email);

        List<Debt> owedToUser = debtRepository.findByCreditorIdAndSettledFalse(user.getId());
        List<Debt> owedByUser = debtRepository.findByDebtorIdAndSettledFalse(user.getId());

        Map<String, Double> netDebts = new HashMap<>();

        for (Debt debt : owedToUser) {
            String debtorId = debt.getDebtorId();
            netDebts.put(debtorId, netDebts.getOrDefault(debtorId, 0.0) + debt.getAmount());
        }

        for (Debt debt : owedByUser) {
            String creditorId = debt.getCreditorId();
            netDebts.put(creditorId, netDebts.getOrDefault(creditorId, 0.0) - debt.getAmount());
        }

        return netDebts.entrySet().stream()
                .filter(entry -> Math.abs(entry.getValue()) > 0.01)
                .map(entry -> {
                    DebtResponse response = new DebtResponse();
                    response.setCreditorId(entry.getKey());
                    try {
                        User creditor = userService.findById(entry.getKey());
                        response.setCreditorName(creditor.getFullName());
                    } catch (Exception e) {
                        response.setCreditorName("Unknown User");
                    }
                    response.setAmount(entry.getValue());
                    return response;
                })
                .collect(Collectors.toList());
    }

    public void settleDebt(String email, SettleDebtRequest request) {
        User user = userService.findByEmail(email);

        Settlement settlement = new Settlement();
        settlement.setPayerId(user.getId());
        settlement.setReceiverId(request.getCreditorId());
        settlement.setAmount(request.getAmount());
        settlement.setPaymentMethod(request.getPaymentMethod());
        settlement.setDescription(request.getDescription());

        settlementRepository.save(settlement);

        List<Debt> debts = debtRepository.findByDebtorIdAndSettledFalse(user.getId())
                .stream()
                .filter(debt -> debt.getCreditorId().equals(request.getCreditorId()))
                .collect(Collectors.toList());

        Double remainingAmount = request.getAmount();
        for (Debt debt : debts) {
            if (remainingAmount <= 0)
                break;

            if (debt.getAmount() <= remainingAmount) {
                remainingAmount -= debt.getAmount();
                debt.setSettled(true);
                debtRepository.save(debt);
            } else {
                debt.setAmount(debt.getAmount() - remainingAmount);
                debtRepository.save(debt);
                remainingAmount = 0.0;
            }
        }

        // Mark related reminders as read
        List<com.example.model.Reminder> reminders = reminderRepository
                .findByReceiverIdOrderByCreatedAtDesc(user.getId());
        for (com.example.model.Reminder reminder : reminders) {
            if (reminder.getSenderId().equals(request.getCreditorId()) && !Boolean.TRUE.equals(reminder.getRead())) {
                reminder.setRead(true);
                reminderRepository.save(reminder);
            }
        }
    }

    public void createDebtsFromExpense(Expense expense) {
        if (expense.getParticipantIds() == null || expense.getParticipantIds().isEmpty()) {
            return;
        }

        String payerId = expense.getPaidBy();
        Double totalAmount = expense.getAmount();
        Map<String, Double> shares = expense.getShares();

        if (shares == null) {
            Double sharePerPerson = totalAmount / expense.getParticipantIds().size();
            shares = new HashMap<>();
            for (String participantId : expense.getParticipantIds()) {
                shares.put(participantId, sharePerPerson);
            }
        }

        for (Map.Entry<String, Double> entry : shares.entrySet()) {
            String participantId = entry.getKey();
            Double shareAmount = entry.getValue();

            if (!participantId.equals(payerId) && shareAmount > 0) {
                Debt debt = new Debt();
                debt.setDebtorId(participantId);
                debt.setCreditorId(payerId);
                debt.setAmount(shareAmount);
                debt.setExpenseId(expense.getId());
                debt.setSettled(false);

                debtRepository.save(debt);
            }
        }
    }
}