package com.portfolio.automation_sut.dto;

import com.portfolio.automation_sut.model.Role;

public class UserDtos {
    public record UserResponse(Long id, String email, Role role) {
    }
}
