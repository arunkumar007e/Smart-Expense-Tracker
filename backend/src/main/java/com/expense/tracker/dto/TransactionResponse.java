package com.expense.tracker.dto;

import com.expense.tracker.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        TransactionType type,
        BigDecimal amount,
        String description,
        LocalDate transactionDate,
        LocalDateTime createdAt,
        CategoryResponse category
) {}