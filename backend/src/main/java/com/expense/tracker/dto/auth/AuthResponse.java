package com.expense.tracker.dto.auth;

public record AuthResponse(
        String token,
        String tokenType,
        UserDTO user
) {
    public static AuthResponse of(String token, UserDTO user) {
        return new AuthResponse(token, "Bearer", user);
    }
}