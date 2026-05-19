package com.qataskmanager.automation_sut.dto;

import com.qataskmanager.automation_sut.model.Role;

public class UserDtos {
    public record UserResponse(Long id, String email, Role role) {
    }
}
