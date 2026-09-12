package com.expense.tracker.service;

import com.expense.tracker.dto.CategoryRequest;
import com.expense.tracker.dto.CategoryResponse;
import com.expense.tracker.entity.Category;
import com.expense.tracker.entity.User;
import com.expense.tracker.enums.TransactionType;
import com.expense.tracker.exception.BusinessException;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.repository.CategoryRepository;
import com.expense.tracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll(Long userId, TransactionType type) {
        List<Category> categories = (type == null)
                ? categoryRepository.findByUserIdOrderByNameAsc(userId)
                : categoryRepository.findByUserIdAndTypeOrderByNameAsc(userId, type);

        return categories.stream().map(this::toResponse).toList();
    }

    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCaseAndUserIdAndType(request.name(), userId, request.type())) {
            throw new BusinessException("A " + request.type().name().toLowerCase()
                    + " category named '" + request.name() + "' already exists.");
        }

        Category category = new Category();
        category.setName(request.name().trim());
        category.setType(request.type());
        category.setColor(request.color());
        category.setUser(new User(userId));
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long userId, Long categoryId, CategoryRequest request) {
        Category category = getOwnedCategory(userId, categoryId);

        boolean nameTaken = categoryRepository.findFirstByUserIdAndNameIgnoreCaseAndType(
                        userId, request.name(), request.type())
                .filter(c -> !c.getId().equals(categoryId))
                .isPresent();
        if (nameTaken) {
            throw new BusinessException("A " + request.type().name().toLowerCase()
                    + " category named '" + request.name() + "' already exists.");
        }

        category.setName(request.name().trim());
        category.setType(request.type());
        category.setColor(request.color());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long userId, Long categoryId) {
        Category category = getOwnedCategory(userId, categoryId);
        if (transactionCountByCategory(categoryId) > 0) {
            throw new BusinessException("Cannot delete category '" + category.getName()
                    + "' because it still has transactions.");
        }
        categoryRepository.delete(category);
    }

    /**
     * Creates the default income/expense categories for a newly registered user.
     * Called from AuthService.register().
     */
    @Transactional
    public void seedDefaultCategories(User user) {
        List<String[]> defaults = List.of(
                new String[]{"Food", "EXPENSE", "#EF4444"},
                new String[]{"Transport", "EXPENSE", "#3B82F6"},
                new String[]{"Shopping", "EXPENSE", "#8B5CF6"},
                new String[]{"Bills", "EXPENSE", "#F59E0B"},
                new String[]{"Entertainment", "EXPENSE", "#EC4899"},
                new String[]{"Education", "EXPENSE", "#14B8A6"},
                new String[]{"Health", "EXPENSE", "#22C55E"},
                new String[]{"Other", "EXPENSE", "#64748B"},
                new String[]{"Salary", "INCOME", "#10B981"},
                new String[]{"Freelance", "INCOME", "#06B6D4"},
                new String[]{"Business", "INCOME", "#6366F1"},
                new String[]{"Other", "INCOME", "#94A3B8"}
        );

        for (String[] d : defaults) {
            Category category = new Category();
            category.setName(d[0]);
            category.setType(TransactionType.valueOf(d[1]));
            category.setColor(d[2]);
            category.setUser(user);
            categoryRepository.save(category);
        }
    }

    private Category getOwnedCategory(Long userId, Long categoryId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
    }

    private long transactionCountByCategory(Long categoryId) {
        return transactionRepository.countByCategoryId(categoryId);
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getType(), category.getColor());
    }
}