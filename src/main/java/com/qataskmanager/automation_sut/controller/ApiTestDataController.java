package com.qataskmanager.automation_sut.controller;

import com.qataskmanager.automation_sut.dto.TestDataDtos.TestDataSummaryResponse;
import com.qataskmanager.automation_sut.service.TestDataService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    public TestDataSummaryResponse reset() {
        return testDataService.reset();
    }

    @Operation(summary = "Clear all SUT data except the minimum admin user. Local/test profiles only. Admin only.")
    @PostMapping("/clear")
    public TestDataSummaryResponse clear() {
        return testDataService.clearAllData();
    }

    @Operation(summary = "Load the bundled deterministic demo data. Local/test profiles only. Admin only.")
    @PostMapping("/demo")
    public TestDataSummaryResponse demo() {
        return testDataService.loadDemoData();
    }

    @Operation(summary = "Import controlled JSON test data. Local/test profiles only. Admin only.")
    @PostMapping("/import")
    public TestDataSummaryResponse importData(@RequestPart("file") MultipartFile file) {
        return testDataService.importData(file);
    }
}
