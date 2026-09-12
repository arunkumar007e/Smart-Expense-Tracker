package com.expense.tracker.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

/** Spending breakdown of a single category within a period. */
public record CategoryTotal(
        String category,
        String color,
        long count,
        BigDecimal total
) {}