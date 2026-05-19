package com.qataskmanager.automation_sut.dto;

import com.qataskmanager.automation_sut.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
    public record LoginRequest(
            @Schema(example = "user1@example.com") @NotBlank @Email String email,
            @Schema(example = "password123") @NotBlank String password
    ) {
    }

    public record LoginResponse(
            String token,
            Long userId,
            String email,
            Role role
    ) {
    }
}
