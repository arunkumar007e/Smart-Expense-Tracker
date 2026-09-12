package com.expense.tracker.service;

import com.expense.tracker.entity.Transaction;
import com.expense.tracker.enums.TransactionType;
import com.expense.tracker.repository.TransactionRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getSummary_calculatesTotalsBalanceAndMonthlySeries() {
        var user = com.expense.tracker.TestData.user(1L, "user@example.com");
        var salary = com.expense.tracker.TestData.category(1L, "Salary", TransactionType.INCOME, user);
        var food = com.expense.tracker.TestData.category(2L, "Food", TransactionType.EXPENSE, user);

        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        LocalDate inMonth = currentMonth.atDay(5);
        YearMonth lastMonth = currentMonth.minusMonths(1);
        LocalDate lastMonthDate = lastMonth.atDay(15);

        List<Transaction> all = List.of(
                com.expense.tracker.TestData.transaction(1L, TransactionType.INCOME, new BigDecimal("5000"), "Salary", inMonth, salary, user),
                com.expense.tracker.TestData.transaction(2L, TransactionType.EXPENSE, new BigDecimal("1200"), "Groceries", inMonth, food, user),
                com.expense.tracker.TestData.transaction(3L, TransactionType.EXPENSE, new BigDecimal("800"), "Dinner", lastMonthDate, food, user)
        );

        when(transactionRepository.findByUserIdOrderByTransactionDateDescIdDesc(1L)).thenReturn(all);

        var summary = dashboardService.getSummary(1L);

        assertThat(summary.totalIncome()).isEqualByComparingTo("5000");
        assertThat(summary.totalExpense()).isEqualByComparingTo("2000");
        assertThat(summary.balance()).isEqualByComparingTo("3000");        // 5000 - 2000
        assertThat(summary.monthlyIncome()).isEqualByComparingTo("5000");
        assertThat(summary.monthlyExpense()).isEqualByComparingTo("1200"); // only current month
        assertThat(summary.totalTransactions()).isEqualTo(3);

        // Expense by category: Food = 2000
        assertThat(summary.categoryBreakdown()).hasSize(1);
        assertThat(summary.categoryBreakdown().get(0).category()).isEqualTo("Food");
        assertThat(summary.categoryBreakdown().get(0).total()).isEqualByComparingTo("2000");

        // Monthly series covers last 6 months, including last month's 800 expense
        assertThat(summary.monthlySeries()).hasSize(6);
        var lastMonthPoint = summary.monthlySeries().stream()
                .filter(p -> p.month().equals(lastMonth))
                .findFirst().orElseThrow();
        assertThat(lastMonthPoint.expense()).isEqualByComparingTo("800");
    }
}