package com.expense.tracker.service;

import com.expense.tracker.dto.CategoryTotal;
import com.expense.tracker.dto.DashboardSummary;
import com.expense.tracker.dto.TransactionResponse;
import com.expense.tracker.entity.Transaction;
import com.expense.tracker.enums.TransactionType;
import com.expense.tracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates the numbers shown on the Dashboard.
 * A personal tracker has a small amount of data, so we load the user's
 * transactions once and do the grouping in Java - simple and portable.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int RECENT_TRANSACTION_LIMIT = 8;
    private static final int MONTHLY_SERIES_MONTHS = 6;

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    @Transactional(readOnly = true)
    public DashboardSummary getSummary(Long userId) {
        List<Transaction> all = transactionRepository.findByUserIdOrderByTransactionDateDescIdDesc(userId);

        BigDecimal totalIncome = sumByType(all, TransactionType.INCOME);
        BigDecimal totalExpense = sumByType(all, TransactionType.EXPENSE);

        YearMonth currentMonth = YearMonth.now();
        BigDecimal monthlyIncome = sumByType(all, TransactionType.INCOME, currentMonth);
        BigDecimal monthlyExpense = sumByType(all, TransactionType.EXPENSE, currentMonth);

        List<TransactionResponse> recent = all.stream()
                .limit(RECENT_TRANSACTION_LIMIT)
                .map(transactionService::toResponse)
                .toList();

        return DashboardSummary.of(
                totalIncome,
                totalExpense,
                monthlyIncome,
                monthlyExpense,
                all.size(),
                recent,
                categoryBreakdown(all),
                monthlySeries(all, currentMonth));
    }

    /**
     * Expense totals grouped by category (all-time), sorted from largest to smallest.
     */
    private List<CategoryTotal> categoryBreakdown(List<Transaction> all) {
        Map<String, CategoryAccumulator> byCategory = new LinkedHashMap<>();

        for (Transaction t : all) {
            if (t.getType() != TransactionType.EXPENSE) {
                continue;
            }
            byCategory.computeIfAbsent(t.getCategory().getName(), n -> new CategoryAccumulator(t.getCategory().getColor()))
                    .add(t.getAmount());
        }

        return byCategory.entrySet().stream()
                .map(e -> new CategoryTotal(e.getKey(), e.getValue().color, e.getValue().count, e.getValue().total))
                .sorted((a, b) -> b.total().compareTo(a.total()))
                .toList();
    }

    /**
     * Income vs expense for the last 6 months (including the current month).
     * Used by the dashboard bar chart.
     */
    private List<DashboardSummary.MonthlyPoint> monthlySeries(List<Transaction> all, YearMonth end) {
        YearMonth start = end.minusMonths(MONTHLY_SERIES_MONTHS - 1);

        Map<YearMonth, DashboardSummary.MonthlyPoint> points = new LinkedHashMap<>();
        for (int i = 0; i < MONTHLY_SERIES_MONTHS; i++) {
            YearMonth m = start.plusMonths(i);
            points.put(m, new DashboardSummary.MonthlyPoint(m, BigDecimal.ZERO, BigDecimal.ZERO));
        }

        for (Transaction t : all) {
            YearMonth m = YearMonth.from(t.getTransactionDate());
            if (m.isBefore(start) || m.isAfter(end)) {
                continue;
            }
            DashboardSummary.MonthlyPoint point = points.get(m);
            BigDecimal newIncome = point.income().add(t.getType() == TransactionType.INCOME ? t.getAmount() : BigDecimal.ZERO);
            BigDecimal newExpense = point.expense().add(t.getType() == TransactionType.EXPENSE ? t.getAmount() : BigDecimal.ZERO);
            points.put(m, new DashboardSummary.MonthlyPoint(m, newIncome, newExpense));
        }

        return new ArrayList<>(points.values());
    }

    private BigDecimal sumByType(List<Transaction> all, TransactionType type) {
        return all.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumByType(List<Transaction> all, TransactionType type, YearMonth month) {
        return all.stream()
                .filter(t -> t.getType() == type && YearMonth.from(t.getTransactionDate()).equals(month))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static final class CategoryAccumulator {
        private final String color;
        private long count;
        private BigDecimal total = BigDecimal.ZERO;

        CategoryAccumulator(String color) {
            this.color = color;
        }

        void add(BigDecimal amount) {
            count++;
            total = total.add(amount);
        }
    }
}