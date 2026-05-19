package com.qataskmanager.automation_sut.dto;

public class DashboardDtos {
    public record DashboardSummaryResponse(
            TaskSummary tasks,
            IssueSummary issues,
            long unreadNotifications
    ) {
    }

    public record TaskSummary(
            long total,
            long todo,
            long inProgress,
            long done,
            long cancelled
    ) {
    }

    public record IssueSummary(
            long total,
            long open,
            long inProgress,
            long blocked,
            long resolved,
            long closed,
            long cancelled
    ) {
    }
}
