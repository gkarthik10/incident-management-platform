package com.karthik.incidentmanagement.controller;

import com.karthik.incidentmanagement.dto.LoginRequestDto;
import com.karthik.incidentmanagement.dto.LoginResponseDto;
import com.karthik.incidentmanagement.dto.RegisterRequestDto;
import com.karthik.incidentmanagement.dto.UserSummaryDto;
import com.karthik.incidentmanagement.security.SecurityUtils;
import com.karthik.incidentmanagement.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDto dto) {
        return ResponseEntity.status(201).body(authService.register(dto));
    }

    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto dto) {
        return authService.login(dto);
    }

    /** Lets the frontend fetch the logged-in user's profile (name, email, role) once it has a JWT. */
    @GetMapping("/me")
    public UserSummaryDto me() {
        return authService.getCurrentUser(SecurityUtils.getCurrentUserEmail());
    }
}
