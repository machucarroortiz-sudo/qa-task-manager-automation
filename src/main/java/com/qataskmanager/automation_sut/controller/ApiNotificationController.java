package com.qataskmanager.automation_sut.controller;

import com.qataskmanager.automation_sut.dto.NotificationDtos.NotificationResponse;
import com.qataskmanager.automation_sut.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class ApiNotificationController {
    private final NotificationService notificationService;

    public ApiNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "List unread notifications for the current user")
    @GetMapping
    public List<NotificationResponse> listUnread() {
        return notificationService.listUnreadForCurrentUser();
    }

    @Operation(summary = "Mark a notification as read")
    @PostMapping("/{id}/read")
    public Map<String, String> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return Map.of("message", "Notification marked as read");
    }
}
