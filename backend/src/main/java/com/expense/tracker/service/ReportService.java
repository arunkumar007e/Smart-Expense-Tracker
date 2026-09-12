package com.expense.tracker.service;

import com.expense.tracker.dto.CategoryTotal;
import com.expense.tracker.dto.MonthlyReport;
import com.expense.tracker.entity.Transaction;
import com.expense.tracker.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Powers the Reports page: selected-month income/expense/balance
 * and an expense breakdown by category.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionService transactionService;

    @Transactional(readOnly = true)
    public MonthlyReport monthlyReport(Long userId, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<Transaction> transactions = transactionService.findOwned(userId, start, end);

        BigDecimal income = sumByType(transactions, TransactionType.INCOME);
        BigDecimal expense = sumByType(transactions, TransactionType.EXPENSE);

        return new MonthlyReport(
                month,
                income,
                expense,
                income.subtract(expense),
                transactions.size(),
                categoryBreakdown(transactions, TransactionType.EXPENSE));
    }

    /**
     * Totals by category within a month for a given transaction type.
     * month defaults to the current month, type to EXPENSE.
     */
    @Transactional(readOnly = true)
    public List<CategoryTotal> categoryBreakdown(Long userId, YearMonth month, TransactionType type) {
        YearMonth target = (month != null) ? month : YearMonth.now();
        TransactionType effectiveType = (type != null) ? type : TransactionType.EXPENSE;
        LocalDate from = target.atDay(1);
        LocalDate to = target.atEndOfMonth();
        return categoryBreakdown(transactionService.findOwned(userId, from, to), effectiveType);
    }

    /** Totals by category for the given list of transactions, filtered by type. */
    private List<CategoryTotal> categoryBreakdown(List<Transaction> transactions, TransactionType type) {
        Map<String, CategoryAccumulator> byCategory = new LinkedHashMap<>();

        for (Transaction t : transactions) {
            if (t.getType() != type) {
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

    private BigDecimal sumByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
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