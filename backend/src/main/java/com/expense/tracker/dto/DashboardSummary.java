package com.expense.tracker.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/** Data shown on the Dashboard page. */
public record DashboardSummary(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        BigDecimal monthlyIncome,
        BigDecimal monthlyExpense,
        long totalTransactions,
        List<TransactionResponse> recentTransactions,
        List<CategoryTotal> categoryBreakdown,
        List<MonthlyPoint> monthlySeries
) {
    /** One point on the "income vs expense" chart, e.g. 2026-09. */
    public record MonthlyPoint(YearMonth month, BigDecimal income, BigDecimal expense) {}

    /** Creates a summary and derives the balance as income minus expense. */
    public static DashboardSummary of(
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal monthlyIncome,
            BigDecimal monthlyExpense,
            long totalTransactions,
            List<TransactionResponse> recentTransactions,
            List<CategoryTotal> categoryBreakdown,
            List<MonthlyPoint> monthlySeries) {
        return new DashboardSummary(
                totalIncome,
                totalExpense,
                totalIncome.subtract(totalExpense),
                monthlyIncome,
                monthlyExpense,
                totalTransactions,
                recentTransactions,
                categoryBreakdown,
                monthlySeries);
    }
}