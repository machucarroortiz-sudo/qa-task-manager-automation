package com.qataskmanager.automation_sut.dto;

public class TestDataDtos {
    public record TestDataSummaryResponse(
            String message,
            long users,
            long tasks,
            long issues,
            long labels,
            long comments
    ) {
    }
}
