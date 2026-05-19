package com.qataskmanager.automation_sut.service;

import com.qataskmanager.automation_sut.model.Role;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ShellModelService {
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public ShellModelService(CurrentUserService currentUserService, NotificationService notificationService) {
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    public void addShellAttributes(Model model) {
        var currentUser = currentUserService.getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAdmin", currentUser.getRole() == Role.ADMIN);
        model.addAttribute("notifications", notificationService.listUnreadForCurrentUser());
        model.addAttribute("unreadNotificationCount", notificationService.countUnreadForCurrentUser());
    }
}
