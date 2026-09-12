package com.expense.tracker.repository;

import com.expense.tracker.entity.Category;
import com.expense.tracker.entity.User;
import com.expense.tracker.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserIdOrderByNameAsc(Long userId);

    List<Category> findByUserIdAndTypeOrderByNameAsc(Long userId, TransactionType type);

    Optional<Category> findByIdAndUserId(Long id, Long userId);

    Optional<Category> findFirstByUserIdAndNameIgnoreCaseAndType(Long userId, String name, TransactionType type);

    boolean existsByNameIgnoreCaseAndUserIdAndType(String name, Long userId, TransactionType type);

    long countByUserId(Long userId);
}