package com.expense.tracker.controller;

import com.expense.tracker.dto.CategoryTotal;
import com.expense.tracker.dto.MonthlyReport;
import com.expense.tracker.enums.TransactionType;
import com.expense.tracker.security.UserPrincipal;
import com.expense.tracker.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Monthly summaries and category breakdowns for the Reports page")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/monthly")
    @Operation(summary = "Income, expense, balance and category breakdown for a selected month (yyyy-MM)")
    public MonthlyReport monthly(@AuthenticationPrincipal UserPrincipal principal,
                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return reportService.monthlyReport(principal.getId(), month);
    }

    @GetMapping("/category")
    @Operation(summary = "Totals by category for a selected month and type (month defaults to the current month, type to EXPENSE)")
    public List<CategoryTotal> category(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(required = false) TransactionType type) {
        return reportService.categoryBreakdown(principal.getId(), month, type);
    }
}