package com.expense.tracker;

import com.expense.tracker.entity.Category;
import com.expense.tracker.entity.Transaction;
import com.expense.tracker.entity.User;
import com.expense.tracker.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Small helpers to build entities quickly in tests. */
public final class TestData {

    private TestData() {
    }

    public static User user(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("$2a$10$abcdefghijklmnopqrstuv"); // fake BCrypt hash
        return user;
    }

    public static Category category(Long id, String name, TransactionType type, User user) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setType(type);
        category.setColor("#EF4444");
        category.setUser(user);
        return category;
    }

    public static Transaction transaction(Long id, TransactionType type, BigDecimal amount,
                                          String description, LocalDate date, Category category, User user) {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setTransactionDate(date);
        transaction.setCategory(category);
        transaction.setUser(user);
        return transaction;
    }
}