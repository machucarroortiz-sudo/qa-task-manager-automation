package com.qataskmanager.automation_sut.controller;

import com.qataskmanager.automation_sut.dto.SutInfoDtos.SutInfoResponse;
import com.qataskmanager.automation_sut.service.SutInfoService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sut-info")
public class ApiSutInfoController {
    private final SutInfoService sutInfoService;

    public ApiSutInfoController(SutInfoService sutInfoService) {
        this.sutInfoService = sutInfoService;
    }

    @Operation(summary = "Return SUT application metadata and runtime information")
    @GetMapping
    public SutInfoResponse getSutInfo() {
        return sutInfoService.getSutInfo();
    }
}
