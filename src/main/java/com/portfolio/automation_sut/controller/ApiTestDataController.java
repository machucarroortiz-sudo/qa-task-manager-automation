package com.portfolio.automation_sut.controller;

import com.portfolio.automation_sut.service.TestDataService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile({"local", "test"})
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/test-data")
public class ApiTestDataController {
    private final TestDataService testDataService;

    public ApiTestDataController(TestDataService testDataService) {
        this.testDataService = testDataService;
    }

    @Operation(summary = "Reset database to predefined seed data. Local/test profiles only. Admin only.")
    @PostMapping("/reset")
    public Map<String, String> reset() {
        testDataService.reset();
        return Map.of("message", "Test data reset completed");
    }
}
