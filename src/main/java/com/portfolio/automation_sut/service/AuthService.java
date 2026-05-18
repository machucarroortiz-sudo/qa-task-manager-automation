package com.portfolio.automation_sut.service;

import com.portfolio.automation_sut.dto.AuthDtos.LoginResponse;
import com.portfolio.automation_sut.model.AppUser;
import com.portfolio.automation_sut.repository.UserRepository;
import com.portfolio.automation_sut.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(String email, String password) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return new LoginResponse(jwtService.createToken(user), user.getId(), user.getEmail(), user.getRole());
    }
}
