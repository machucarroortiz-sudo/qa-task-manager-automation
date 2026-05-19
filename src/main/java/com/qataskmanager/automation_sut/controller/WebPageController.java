package com.qataskmanager.automation_sut.controller;

import com.qataskmanager.automation_sut.model.Role;
import com.qataskmanager.automation_sut.service.CurrentUserService;
import com.qataskmanager.automation_sut.service.IssueService;
import com.qataskmanager.automation_sut.service.NotificationService;
import com.qataskmanager.automation_sut.service.ShellModelService;
import com.qataskmanager.automation_sut.service.TaskService;
import java.util.Comparator;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class WebPageController {
    private final TaskService taskService;
    private final IssueService issueService;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;
    private final ShellModelService shellModelService;

    public WebPageController(TaskService taskService, IssueService issueService, CurrentUserService currentUserService,
                             NotificationService notificationService, ShellModelService shellModelService) {
        this.taskService = taskService;
        this.issueService = issueService;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
        this.shellModelService = shellModelService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        var tasks = taskService.listVisibleTasks();
        var issues = issueService.listVisibleIssues();
        long taskCount = tasks.size();
        long taskTodoCount = tasks.stream().filter(task -> task.status().name().equals("TODO")).count();
        long taskInProgressCount = tasks.stream().filter(task -> task.status().name().equals("IN_PROGRESS")).count();
        long taskDoneCount = tasks.stream().filter(task -> task.status().name().equals("DONE")).count();
        long issueCount = issues.size();
        long issueOpenCount = issues.stream().filter(issue -> issue.status().name().equals("OPEN")).count();
        long issueInProgressCount = issues.stream().filter(issue -> issue.status().name().equals("IN_PROGRESS")).count();
        long issueBlockedCount = issues.stream().filter(issue -> issue.status().name().equals("BLOCKED")).count();
        long issueResolvedCount = issues.stream().filter(issue -> issue.status().name().equals("RESOLVED")).count();
        long issueClosedCount = issues.stream().filter(issue -> issue.status().name().equals("CLOSED")).count();
        long issueCancelledCount = issues.stream().filter(issue -> issue.status().name().equals("CANCELLED")).count();
        var currentUser = currentUserService.getCurrentUser();

        model.addAttribute("taskCount", taskCount);
        model.addAttribute("taskTodoCount", taskTodoCount);
        model.addAttribute("taskInProgressCount", taskInProgressCount);
        model.addAttribute("taskDoneCount", taskDoneCount);
        model.addAttribute("taskTodoPercent", percent(taskTodoCount, taskCount));
        model.addAttribute("taskInProgressPercent", percent(taskInProgressCount, taskCount));
        model.addAttribute("taskDonePercent", percent(taskDoneCount, taskCount));
        model.addAttribute("issueCount", issueCount);
        model.addAttribute("issueOpenCount", issueOpenCount);
        model.addAttribute("issueInProgressCount", issueInProgressCount);
        model.addAttribute("issueBlockedCount", issueBlockedCount);
        model.addAttribute("issueResolvedCount", issueResolvedCount);
        model.addAttribute("issueClosedCount", issueClosedCount);
        model.addAttribute("issueCancelledCount", issueCancelledCount);
        model.addAttribute("issueOpenPercent", percent(issueOpenCount, issueCount));
        model.addAttribute("issueInProgressPercent", percent(issueInProgressCount, issueCount));
        model.addAttribute("issueBlockedPercent", percent(issueBlockedCount, issueCount));
        model.addAttribute("issueResolvedPercent", percent(issueResolvedCount, issueCount));
        model.addAttribute("issueClosedPercent", percent(issueClosedCount, issueCount));
        model.addAttribute("issueCancelledPercent", percent(issueCancelledCount, issueCount));
        model.addAttribute("recentTasks", tasks.stream().limit(4).toList());
        model.addAttribute("recentIssues", issues.stream().limit(4).toList());
        model.addAttribute("upcomingTasks", tasks.stream()
                .sorted(Comparator.comparing(task -> task.dueDate()))
                .limit(3)
                .toList());
        model.addAttribute("notifications", notificationService.listUnreadForCurrentUser());
        model.addAttribute("unreadNotificationCount", notificationService.countUnreadForCurrentUser());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAdmin", currentUser.getRole() == Role.ADMIN);
        return "dashboard";
    }

    @PostMapping("/dashboard/notifications/{id}/read")
    public String markNotificationRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        var user = currentUserService.getCurrentUser();
        model.addAttribute("user", user);
        shellModelService.addShellAttributes(model);
        return "profile";
    }

    private int percent(long value, long total) {
        if (total == 0) {
            return 0;
        }
        return (int) Math.round((value * 100.0) / total);
    }
}
