package com.qataskmanager.automation_sut.controller;

import com.qataskmanager.automation_sut.dto.AuthDtos.LoginRequest;
import com.qataskmanager.automation_sut.dto.AuthDtos.LoginResponse;
import com.qataskmanager.automation_sut.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {
    private final AuthService authService;

    public ApiAuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Login and receive a JWT",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authenticated"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(examples = @ExampleObject(value = """
                            {"status":401,"error":"Unauthorized","message":"Invalid email or password","validationErrors":{}}
                            """)))
            }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.email(), request.password()));
    }
}
