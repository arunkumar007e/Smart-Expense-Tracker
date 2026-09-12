package com.expense.tracker.dto;

import com.expense.tracker.enums.TransactionType;

public record CategoryResponse(
        Long id,
        String name,
        TransactionType type,
        String color
) {}