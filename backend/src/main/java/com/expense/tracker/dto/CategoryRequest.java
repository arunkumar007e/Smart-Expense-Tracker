package com.expense.tracker.dto;

import com.expense.tracker.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Category name is required")
        @Size(max = 50, message = "Category name must be at most 50 characters")
        String name,

        @NotNull(message = "Category type is required")
        TransactionType type,

        @NotBlank(message = "Color is required")
        @Size(min = 7, max = 7, message = "Color must be a hex code like #FF5733")
        String color
) {}