package com.expense.tracker.controller;

import com.expense.tracker.dto.TransactionRequest;
import com.expense.tracker.dto.TransactionResponse;
import com.expense.tracker.enums.TransactionType;
import com.expense.tracker.security.UserPrincipal;
import com.expense.tracker.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

/**
 * CRUD for the authenticated user's transactions.
 * The current user is always resolved from the JWT (@AuthenticationPrincipal),
 * and every query is scoped by userId - users can never see each other's data.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Income/expense transactions (authenticated)")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "List transactions with optional filters (type, category, month, search)")
    public List<TransactionResponse> getAll(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(required = false) String search) {
        return transactionService.findAll(principal.getId(), type, categoryId, month, search);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single transaction")
    public TransactionResponse getById(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable Long id) {
        return transactionService.findById(principal.getId(), id);
    }

    @PostMapping
    @Operation(summary = "Create a transaction")
    public ResponseEntity<TransactionResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.create(principal.getId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a transaction")
    public TransactionResponse update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        return transactionService.update(principal.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable Long id) {
        transactionService.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}