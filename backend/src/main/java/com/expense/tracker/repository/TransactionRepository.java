package com.expense.tracker.repository;

import com.expense.tracker.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByTransactionDateDescIdDesc(Long userId);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    void deleteByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByCategoryId(Long categoryId);
}