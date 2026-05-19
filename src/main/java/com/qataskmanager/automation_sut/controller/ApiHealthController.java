package com.qataskmanager.automation_sut.controller;

import com.qataskmanager.automation_sut.dto.HealthDtos.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import javax.sql.DataSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class ApiHealthController {
    private final DataSource dataSource;

    public ApiHealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Operation(summary = "Return application and database health")
    @GetMapping
    public HealthResponse health() {
        try (var connection = dataSource.getConnection()) {
            return new HealthResponse("UP", connection.isValid(2) ? "UP" : "DOWN");
        } catch (Exception exception) {
            return new HealthResponse("DOWN", "DOWN");
        }
    }
}
