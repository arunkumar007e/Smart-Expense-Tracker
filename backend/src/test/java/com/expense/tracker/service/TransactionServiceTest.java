package com.expense.tracker.service;

import com.expense.tracker.dto.TransactionRequest;
import com.expense.tracker.entity.Category;
import com.expense.tracker.entity.Transaction;
import com.expense.tracker.entity.User;
import com.expense.tracker.enums.TransactionType;
import com.expense.tracker.exception.BusinessException;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.repository.CategoryRepository;
import com.expense.tracker.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Category expenseCategory;
    private Category incomeCategory;

    @BeforeEach
    void setUp() {
        user = com.expense.tracker.TestData.user(1L, "user@example.com");
        expenseCategory = com.expense.tracker.TestData.category(10L, "Food", TransactionType.EXPENSE, user);
        incomeCategory = com.expense.tracker.TestData.category(11L, "Salary", TransactionType.INCOME, user);
    }

    @Test
    void create_withValidExpense_savesAndReturnsTransaction() {
        TransactionRequest request = new TransactionRequest(
                TransactionType.EXPENSE,
                new BigDecimal("250.50"),
                expenseCategory.getId(),
                LocalDate.of(2026, 9, 10),
                "Lunch at cafe");

        Transaction saved = com.expense.tracker.TestData.transaction(
                1L, TransactionType.EXPENSE, new BigDecimal("250.50"),
                "Lunch at cafe", LocalDate.of(2026, 9, 10), expenseCategory, user);

        when(categoryRepository.findByIdAndUserId(expenseCategory.getId(), user.getId()))
                .thenReturn(Optional.of(expenseCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        var response = transactionService.create(user.getId(), request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(response.amount()).isEqualByComparingTo("250.50");
        assertThat(response.category().name()).isEqualTo("Food");
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void create_withCategoryOfWrongType_throwsBusinessException() {
        TransactionRequest request = new TransactionRequest(
                TransactionType.EXPENSE,
                new BigDecimal("100"),
                incomeCategory.getId(), // income category used for an expense
                LocalDate.of(2026, 9, 10),
                "Rent");

        when(categoryRepository.findByIdAndUserId(incomeCategory.getId(), user.getId()))
                .thenReturn(Optional.of(incomeCategory));

        assertThatThrownBy(() -> transactionService.create(user.getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expense");
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void getByForeignId_otherUsersTransaction_throws404() {
        // Category and transaction belong to user 2, but we query as user 1.
        User other = com.expense.tracker.TestData.user(2L, "other@example.com");
        Category otherCategory = com.expense.tracker.TestData.category(99L, "Other", TransactionType.EXPENSE, other);
        when(transactionRepository.findByIdAndUserId(1L, user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.findById(user.getId(), 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void update_withForeignId_throws404() {
        TransactionRequest request = new TransactionRequest(
                TransactionType.EXPENSE, new BigDecimal("50"), expenseCategory.getId(),
                LocalDate.now(), "x");

        when(transactionRepository.findByIdAndUserId(1L, user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.update(user.getId(), 1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_withOwnedId_callsDeleteByIdAndUserId() {
        Transaction owned = com.expense.tracker.TestData.transaction(
                1L, TransactionType.EXPENSE, new BigDecimal("10"),
                "x", LocalDate.now(), expenseCategory, user);

        when(transactionRepository.findByIdAndUserId(1L, user.getId())).thenReturn(Optional.of(owned));

        transactionService.delete(user.getId(), 1L);

        verify(transactionRepository).deleteByIdAndUserId(1L, user.getId());
    }

    @Test
    void findAll_appliesFilters_ignoresOtherUsersData() {
        User other = com.expense.tracker.TestData.user(2L, "other@example.com");
        Category otherCategory = com.expense.tracker.TestData.category(90L, "Travel", TransactionType.EXPENSE, other);

        Transaction mine = com.expense.tracker.TestData.transaction(
                1L, TransactionType.EXPENSE, new BigDecimal("10"), "Lunch",
                LocalDate.of(2026, 9, 10), expenseCategory, user);
        Transaction theirs = com.expense.tracker.TestData.transaction(
                2L, TransactionType.EXPENSE, new BigDecimal("999"), "Private trip",
                LocalDate.of(2026, 9, 10), otherCategory, other);

        // Repository is user-scoped - it only ever returns the current user's rows.
        when(transactionRepository.findByUserIdOrderByTransactionDateDescIdDesc(user.getId()))
                .thenReturn(java.util.List.of(mine));

        var result = transactionService.findAll(user.getId(), TransactionType.EXPENSE, null, null, "l");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).amount()).isEqualByComparingTo("10");
    }
}