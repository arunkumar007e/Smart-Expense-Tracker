package com.expense.tracker.controller;

import com.expense.tracker.dto.auth.AuthResponse;
import com.expense.tracker.dto.auth.LoginRequest;
import com.expense.tracker.dto.auth.RegisterRequest;
import com.expense.tracker.dto.auth.UserDTO;
import com.expense.tracker.security.UserPrincipal;
import com.expense.tracker.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Public endpoints for registering and logging in")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new account (default categories are created automatically)")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT token")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Get the profile of the currently authenticated user (used to restore the session)")
    public UserDTO me(@AuthenticationPrincipal UserPrincipal principal) {
        return new UserDTO(principal.getId(), principal.getName(), principal.getEmail());
    }
}