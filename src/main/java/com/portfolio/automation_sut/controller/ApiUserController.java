package com.portfolio.automation_sut.controller;

import com.portfolio.automation_sut.dto.UserDtos.UserResponse;
import com.portfolio.automation_sut.model.AppUser;
import com.portfolio.automation_sut.repository.UserRepository;
import com.portfolio.automation_sut.service.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class ApiUserController {
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    public ApiUserController(CurrentUserService currentUserService, UserRepository userRepository) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Return the currently authenticated user")
    @GetMapping("/me")
    public UserResponse me() {
        AppUser user = currentUserService.getCurrentUser();
        return new UserResponse(user.getId(), user.getEmail(), user.getRole());
    }

    @Operation(summary = "Return all users. Admin only.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponse> users() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponse(user.getId(), user.getEmail(), user.getRole()))
                .toList();
    }
}
