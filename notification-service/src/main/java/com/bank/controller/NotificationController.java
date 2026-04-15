package com.bank.controller;

import com.bank.dto.NotificationRequestDTO;
import com.bank.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /*@PostMapping("/account-create-notification")
    public void sendNotification(@RequestBody NotificationRequestDTO dto) {
        notificationService.sendAccountCreationNotification(dto);
    }*/
}
