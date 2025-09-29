package com.example.service;

import java.util.List;
import java.util.stream.Collectors;

import com.example.dto.ReminderRequest;
import com.example.dto.ReminderResponse;
import com.example.model.Reminder;
import com.example.model.User;
import com.example.repository.ReminderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReminderService {

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private UserService userService;

    public List<ReminderResponse> getUserReminders(String email) {
        User user = userService.findByEmail(email);
        List<Reminder> reminders = reminderRepository.findByReceiverIdOrderByCreatedAtDesc(user.getId());

        return reminders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public void sendReminder(String email, ReminderRequest request) {
        User sender = userService.findByEmail(email);

        Reminder reminder = new Reminder();
        reminder.setSenderId(sender.getId());
        reminder.setReceiverId(request.getDebtorId());
        reminder.setMessage(request.getMessage());
        reminder.setMethod(request.getMethod());
        reminder.setRead(false);

        reminderRepository.save(reminder);
    }

    public void markAsRead(String id) {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new com.example.exception.ResourceNotFoundException("Reminder not found"));
        reminder.setRead(true);
        reminderRepository.save(reminder);
    }

    public void markAllAsRead(String email) {
        User user = userService.findByEmail(email);
        List<Reminder> reminders = reminderRepository.findByReceiverIdOrderByCreatedAtDesc(user.getId());

        reminders.forEach(reminder -> reminder.setRead(true));
        reminderRepository.saveAll(reminders);
    }

    private ReminderResponse convertToResponse(Reminder reminder) {
        ReminderResponse response = new ReminderResponse();
        response.setId(reminder.getId());
        response.setMessage(reminder.getMessage());
        response.setRead(reminder.getRead());
        response.setCreatedAt(reminder.getCreatedAt());

        try {
            User sender = userService.findById(reminder.getSenderId());
            response.setSenderName(sender.getFullName());
        } catch (Exception e) {
            response.setSenderName("Unknown User");
        }

        return response;
    }
}
