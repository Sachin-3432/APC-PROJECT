
package com.example.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.dto.GroupRequest;
import com.example.dto.GroupResponse;
import com.example.model.Group;
import com.example.model.User;
import com.example.repository.GroupRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private com.example.repository.ExpenseRepository expenseRepository;

    @Autowired
    private com.example.repository.DebtRepository debtRepository;

    @Autowired
    private com.example.repository.UserRepository userRepository;

    @Autowired
    private com.example.repository.SettlementRepository settlementRepository;

    public java.util.Map<String, Object> getGroupBalances(String groupId) {
        // Get group and members
        Group group = findById(groupId);
        List<String> memberIds = group.getMemberIds();

        // Get all debts in this group
        java.util.List<com.example.model.Debt> debts = debtRepository.findAll();
        java.util.Map<String, Double> balances = new java.util.HashMap<>();
        for (String memberId : memberIds) {
            balances.put(memberId, 0.0);
        }
        for (com.example.model.Debt debt : debts) {
            if (debt.getExpenseId() != null) {
                com.example.model.Expense expense = expenseRepository.findById(debt.getExpenseId()).orElse(null);
                if (expense != null && groupId.equals(expense.getGroupId())) {
                    // Debtor owes creditor
                    if (balances.containsKey(debt.getDebtorId())) {
                        balances.put(debt.getDebtorId(), balances.get(debt.getDebtorId()) - debt.getAmount());
                    }
                    if (balances.containsKey(debt.getCreditorId())) {
                        balances.put(debt.getCreditorId(), balances.get(debt.getCreditorId()) + debt.getAmount());
                    }
                }
            }
        }

        // Subtract settlements between group members
        java.util.List<com.example.model.Settlement> settlements = settlementRepository.findAll();
        for (com.example.model.Settlement settlement : settlements) {
            String payerId = settlement.getPayerId();
            String receiverId = settlement.getReceiverId();
            Double amount = settlement.getAmount();
            // Only consider settlements between group members
            if (memberIds.contains(payerId) && memberIds.contains(receiverId)) {
                // Payer paid receiver, so payer's balance increases, receiver's decreases
                balances.put(payerId, balances.getOrDefault(payerId, 0.0) + amount);
                balances.put(receiverId, balances.getOrDefault(receiverId, 0.0) - amount);
            }
        }

        // Map memberIds to full names
        java.util.Map<String, String> memberNames = new java.util.HashMap<>();
        for (String memberId : memberIds) {
            com.example.model.User user = userRepository.findById(memberId).orElse(null);
            memberNames.put(memberId, user != null ? user.getFullName() : memberId);
        }
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("balances", balances);
        result.put("memberNames", memberNames);
        return result;
    }

    public List<GroupResponse> getUserGroups(String email) {
        User user = userService.findByEmail(email);
        List<Group> groups = groupRepository.findByMemberIdsContaining(user.getId());

        return groups.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public GroupResponse createGroup(String email, GroupRequest request) {
        User user = userService.findByEmail(email);

        Group group = new Group();
        group.setName(request.getName());
        group.setCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        group.setCreatedBy(user.getId());
        group.getMemberIds().add(user.getId());

        group = groupRepository.save(group);
        return convertToResponse(group);
    }

    public com.example.dto.JoinResponse joinGroup(String email, String groupCode) {
        User user = userService.findByEmail(email);
        Group group = groupRepository.findByCode(groupCode)
                .orElseThrow(() -> new com.example.exception.ResourceNotFoundException("Group not found"));

        // Prevent owner from joining their own group
        if (group.getCreatedBy() != null && group.getCreatedBy().equals(user.getId())) {
            throw new com.example.exception.BadRequestException("You are the owner of this group and cannot join it.");
        }

        boolean already = group.getMemberIds().contains(user.getId());
        if (!already) {
            group.getMemberIds().add(user.getId());
            group = groupRepository.save(group);
        }

        com.example.dto.JoinResponse resp = new com.example.dto.JoinResponse();
        resp.setGroup(convertToResponse(group));
        resp.setAlreadyMember(already);
        return resp;
    }

    public GroupResponse updateGroup(String id, GroupRequest request) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new com.example.exception.ResourceNotFoundException("Group not found"));

        group.setName(request.getName());
        group = groupRepository.save(group);
        return convertToResponse(group);
    }

    public void deleteGroup(String id) {
        groupRepository.deleteById(id);
    }

    public GroupResponse getGroupById(String id) {
        Group group = findById(id);
        return convertToResponse(group);
    }

    public Group findById(String id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new com.example.exception.ResourceNotFoundException("Group not found"));
    }

    private GroupResponse convertToResponse(Group group) {
        GroupResponse response = new GroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setCode(group.getCode());
        response.setCreatedBy(group.getCreatedBy());

        List<String> memberNames = group.getMemberIds().stream()
                .map(memberId -> {
                    try {
                        return userService.findById(memberId).getFullName();
                    } catch (Exception e) {
                        return "Unknown User";
                    }
                })
                .collect(Collectors.toList());
        response.setMemberNames(memberNames);
        response.setMemberIds(group.getMemberIds());

        return response;
    }
}
