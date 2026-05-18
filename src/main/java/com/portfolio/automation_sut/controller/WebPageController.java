package com.portfolio.automation_sut.controller;

import com.portfolio.automation_sut.model.Role;
import com.portfolio.automation_sut.service.CurrentUserService;
import com.portfolio.automation_sut.service.IssueService;
import com.portfolio.automation_sut.service.TaskService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebPageController {
    private final TaskService taskService;
    private final IssueService issueService;
    private final CurrentUserService currentUserService;

    public WebPageController(TaskService taskService, IssueService issueService, CurrentUserService currentUserService) {
        this.taskService = taskService;
        this.issueService = issueService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        var tasks = taskService.listVisibleTasks();
        var issues = issueService.listVisibleIssues();
        model.addAttribute("taskCount", tasks.size());
        model.addAttribute("taskTodoCount", tasks.stream().filter(task -> task.status().name().equals("TODO")).count());
        model.addAttribute("taskInProgressCount", tasks.stream().filter(task -> task.status().name().equals("IN_PROGRESS")).count());
        model.addAttribute("taskDoneCount", tasks.stream().filter(task -> task.status().name().equals("DONE")).count());
        model.addAttribute("issueCount", issues.size());
        model.addAttribute("issueOpenCount", issues.stream().filter(issue -> issue.status().name().equals("OPEN")).count());
        model.addAttribute("issueInProgressCount", issues.stream().filter(issue -> issue.status().name().equals("IN_PROGRESS")).count());
        model.addAttribute("issueBlockedCount", issues.stream().filter(issue -> issue.status().name().equals("BLOCKED")).count());
        model.addAttribute("issueResolvedCount", issues.stream().filter(issue -> issue.status().name().equals("RESOLVED")).count());
        model.addAttribute("issueClosedCount", issues.stream().filter(issue -> issue.status().name().equals("CLOSED")).count());
        model.addAttribute("issueCancelledCount", issues.stream().filter(issue -> issue.status().name().equals("CANCELLED")).count());
        model.addAttribute("isAdmin", currentUserService.getCurrentUser().getRole() == Role.ADMIN);
        return "dashboard";
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
        return "profile";
    }
}
