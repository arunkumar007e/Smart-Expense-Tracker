package com.expense.tracker.controller;

import com.expense.tracker.dto.CategoryRequest;
import com.expense.tracker.dto.CategoryResponse;
import com.expense.tracker.enums.TransactionType;
import com.expense.tracker.security.UserPrincipal;
import com.expense.tracker.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "CRUD for the authenticated user's categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "List categories (optionally filter by INCOME or EXPENSE)")
    public List<CategoryResponse> getAll(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) TransactionType type) {
        return categoryService.findAll(principal.getId(), type);
    }

    @PostMapping
    @Operation(summary = "Create a category")
    public ResponseEntity<CategoryResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(principal.getId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a category")
    public CategoryResponse update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(principal.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category (fails if it still has transactions)")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable Long id) {
        categoryService.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}