package com.expense.tracker.service;

import com.expense.tracker.entity.Transaction;
import com.expense.tracker.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private ReportService reportService;

    @Test
    void monthlyReport_groupsIncomeExpenseAndCategoryTotals() {
        var user = com.expense.tracker.TestData.user(1L, "user@example.com");
        var salary = com.expense.tracker.TestData.category(1L, "Salary", TransactionType.INCOME, user);
        var food = com.expense.tracker.TestData.category(2L, "Food", TransactionType.EXPENSE, user);

        YearMonth month = YearMonth.of(2026, 9);
        List<Transaction> monthTransactions = List.of(
                com.expense.tracker.TestData.transaction(1L, TransactionType.INCOME, new BigDecimal("5000"), "Salary", month.atDay(1), salary, user),
                com.expense.tracker.TestData.transaction(2L, TransactionType.EXPENSE, new BigDecimal("1500"), "Groceries", month.atDay(5), food, user),
                com.expense.tracker.TestData.transaction(3L, TransactionType.EXPENSE, new BigDecimal("500"), "Snacks", month.atDay(20), food, user)
        );

        when(transactionService.findOwned(any(), any(), any())).thenReturn(monthTransactions);

        var report = reportService.monthlyReport(1L, month);

        assertThat(report.month()).isEqualTo(month);
        assertThat(report.totalIncome()).isEqualByComparingTo("5000");
        assertThat(report.totalExpense()).isEqualByComparingTo("2000");
        assertThat(report.balance()).isEqualByComparingTo("3000");
        assertThat(report.transactionCount()).isEqualTo(3);

        assertThat(report.categoryBreakdown()).hasSize(1);
        assertThat(report.categoryBreakdown().get(0).category()).isEqualTo("Food");
        assertThat(report.categoryBreakdown().get(0).total()).isEqualByComparingTo("2000");
        assertThat(report.categoryBreakdown().get(0).count()).isEqualTo(2);
    }
}