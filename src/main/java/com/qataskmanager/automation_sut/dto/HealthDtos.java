package com.qataskmanager.automation_sut.dto;

public class HealthDtos {
    public record HealthResponse(String status, String database) {
    }
}
