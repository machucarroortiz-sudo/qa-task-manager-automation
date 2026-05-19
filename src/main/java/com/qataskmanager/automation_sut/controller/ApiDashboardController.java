package com.qataskmanager.automation_sut.controller;

import com.qataskmanager.automation_sut.dto.DashboardDtos.DashboardSummaryResponse;
import com.qataskmanager.automation_sut.dto.DashboardDtos.IssueSummary;
import com.qataskmanager.automation_sut.dto.DashboardDtos.TaskSummary;
import com.qataskmanager.automation_sut.service.IssueService;
import com.qataskmanager.automation_sut.service.NotificationService;
import com.qataskmanager.automation_sut.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class ApiDashboardController {
    private final TaskService taskService;
    private final IssueService issueService;
    private final NotificationService notificationService;

    public ApiDashboardController(TaskService taskService, IssueService issueService, NotificationService notificationService) {
        this.taskService = taskService;
        this.issueService = issueService;
        this.notificationService = notificationService;
    }

    @Operation(summary = "Return dashboard counters visible to the current user")
    @GetMapping("/summary")
    public DashboardSummaryResponse summary() {
        var tasks = taskService.listVisibleTasks();
        var issues = issueService.listVisibleIssues();
        return new DashboardSummaryResponse(
                new TaskSummary(
                        tasks.size(),
                        tasks.stream().filter(task -> task.status().name().equals("TODO")).count(),
                        tasks.stream().filter(task -> task.status().name().equals("IN_PROGRESS")).count(),
                        tasks.stream().filter(task -> task.status().name().equals("DONE")).count(),
                        tasks.stream().filter(task -> task.status().name().equals("CANCELLED")).count()
                ),
                new IssueSummary(
                        issues.size(),
                        issues.stream().filter(issue -> issue.status().name().equals("OPEN")).count(),
                        issues.stream().filter(issue -> issue.status().name().equals("IN_PROGRESS")).count(),
                        issues.stream().filter(issue -> issue.status().name().equals("BLOCKED")).count(),
                        issues.stream().filter(issue -> issue.status().name().equals("RESOLVED")).count(),
                        issues.stream().filter(issue -> issue.status().name().equals("CLOSED")).count(),
                        issues.stream().filter(issue -> issue.status().name().equals("CANCELLED")).count()
                ),
                notificationService.countUnreadForCurrentUser()
        );
    }
}
