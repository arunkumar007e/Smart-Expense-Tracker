package com.expense.tracker.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/** Summary of one month shown on the Reports page. */
public record MonthlyReport(
        YearMonth month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        long transactionCount,
        List<CategoryTotal> categoryBreakdown
) {}