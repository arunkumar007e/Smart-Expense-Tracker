package com.expense.tracker.service;

import com.expense.tracker.dto.CategoryResponse;
import com.expense.tracker.dto.TransactionRequest;
import com.expense.tracker.dto.TransactionResponse;
import com.expense.tracker.entity.Category;
import com.expense.tracker.entity.Transaction;
import com.expense.tracker.entity.User;
import com.expense.tracker.enums.TransactionType;
import com.expense.tracker.exception.BusinessException;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.repository.CategoryRepository;
import com.expense.tracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Returns the current user's transactions filtered by optional filters
     * (type, category, month, free-text description search).
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> findAll(Long userId,
                                             TransactionType type,
                                             Long categoryId,
                                             YearMonth month,
                                             String search) {
        return transactionRepository.findByUserIdOrderByTransactionDateDescIdDesc(userId).stream()
                .filter(t -> type == null || t.getType() == type)
                .filter(t -> categoryId == null || t.getCategory().getId().equals(categoryId))
                .filter(t -> month == null || YearMonth.from(t.getTransactionDate()).equals(month))
                .filter(t -> search == null || search.isBlank()
                        || t.getDescription().toLowerCase(Locale.ROOT)
                            .contains(search.toLowerCase(Locale.ROOT)))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse findById(Long userId, Long transactionId) {
        return toResponse(getOwnedTransaction(userId, transactionId));
    }

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest request) {
        Category category = getOwnedCategory(userId, request.categoryId());
        validateCategoryType(category, request.type());

        Transaction transaction = new Transaction();
        transaction.setUser(new User(userId));
        applyRequest(transaction, request, category);

        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse update(Long userId, Long transactionId, TransactionRequest request) {
        Transaction transaction = getOwnedTransaction(userId, transactionId);

        // Category switch must keep the transaction type consistent.
        if (!transaction.getCategory().getId().equals(request.categoryId())) {
            Category category = getOwnedCategory(userId, request.categoryId());
            validateCategoryType(category, request.type());
            transaction.setCategory(category);
        } else {
            validateCategoryType(transaction.getCategory(), request.type());
        }

        applyRequest(transaction, request, transaction.getCategory());
        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public void delete(Long userId, Long transactionId) {
        getOwnedTransaction(userId, transactionId); // throws 404 when the transaction is not owned by this user
        transactionRepository.deleteByIdAndUserId(transactionId, userId);
    }

    private void applyRequest(Transaction transaction, TransactionRequest request, Category category) {
        transaction.setType(request.type());
        transaction.setAmount(request.amount());
        transaction.setDescription(request.description().trim());
        transaction.setTransactionDate(request.transactionDate());
        transaction.setCategory(category);
    }

    private Transaction getOwnedTransaction(Long userId, Long transactionId) {
        return transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));
    }

    private Category getOwnedCategory(Long userId, Long categoryId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
    }

    /** An EXPENSE transaction cannot use an INCOME category (and vice versa). */
    private void validateCategoryType(Category category, TransactionType type) {
        if (category.getType() != type) {
            String message = type == TransactionType.INCOME
                    ? "Cannot use an income category for an expense transaction."
                    : "Cannot use an expense category for an income transaction.";
            throw new BusinessException(message);
        }
    }

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                transaction.getCreatedAt(),
                new CategoryResponse(
                        transaction.getCategory().getId(),
                        transaction.getCategory().getName(),
                        transaction.getCategory().getType(),
                        transaction.getCategory().getColor()));
    }

    // Exposed to DashboardService/ReportService for in-memory aggregation.
    List<Transaction> findOwned(Long userId, LocalDate start, LocalDate end) {
        return transactionRepository.findByUserIdOrderByTransactionDateDescIdDesc(userId).stream()
                .filter(t -> !t.getTransactionDate().isBefore(start))
                .filter(t -> !t.getTransactionDate().isAfter(end))
                .toList();
    }
}