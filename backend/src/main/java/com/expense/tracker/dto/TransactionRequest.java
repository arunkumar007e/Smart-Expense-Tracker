package com.expense.tracker.dto;

import com.expense.tracker.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull(message = "Type is required (INCOME or EXPENSE)")
        TransactionType type,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotNull(message = "Category is required")
        Long categoryId,

        @NotNull(message = "Transaction date is required")
        @PastOrPresent(message = "Transaction date cannot be in the future")
        LocalDate transactionDate,

        @NotBlank(message = "Description is required")
        @Size(max = 200, message = "Description must be at most 200 characters")
        String description
) {}