package com.expense.tracker.dto.auth;

public record UserDTO(
        Long id,
        String name,
        String email
) {}