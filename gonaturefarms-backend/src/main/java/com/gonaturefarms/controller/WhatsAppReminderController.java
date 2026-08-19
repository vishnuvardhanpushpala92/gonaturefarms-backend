package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.whatsapp.WhatsAppReminderRequest;
import com.gonaturefarms.security.SecurityUtils;
import com.gonaturefarms.service.WhatsAppReminderService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/whatsapp")
@PreAuthorize("hasRole('ADMIN')")
public class WhatsAppReminderController {

    private final WhatsAppReminderService reminderService;

    public WhatsAppReminderController(WhatsAppReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping("/reminders")
    public ApiResponse getAllReminders() {
        Long adminId = SecurityUtils.requireCurrentUser().id();
        return reminderService.getAllReminders(adminId);
    }

    @PostMapping("/create")
    public ApiResponse createReminder(@Valid @RequestBody WhatsAppReminderRequest request) {
        Long adminId = SecurityUtils.requireCurrentUser().id();
        return reminderService.createReminder(adminId, request);
    }

    @PostMapping("/send/{reminderId}")
    public ApiResponse sendReminder(@PathVariable Long reminderId) {
        return reminderService.sendReminder(reminderId);
    }

    @PostMapping("/send-customers")
    public ApiResponse sendToCustomers(@Valid @RequestBody WhatsAppReminderRequest request) {
        Long adminId = SecurityUtils.requireCurrentUser().id();
        return reminderService.sendToCustomers(adminId, request);
    }

    @PostMapping("/generate-product-reminder/{productId}")
    public ApiResponse generateProductReminder(@PathVariable Long productId, @RequestParam String productName) {
        Long adminId = SecurityUtils.requireCurrentUser().id();
        return reminderService.generateProductReminder(adminId, productId, productName);
    }

    @DeleteMapping("/reminders/{reminderId}")
    public ApiResponse deleteReminder(@PathVariable Long reminderId) {
        return reminderService.deleteReminder(reminderId);
    }
}
