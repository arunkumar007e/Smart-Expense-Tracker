package com.expense.tracker.controller;

import com.expense.tracker.dto.DashboardSummary;
import com.expense.tracker.security.UserPrincipal;
import com.expense.tracker.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Aggregated totals and charts for the Dashboard page")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Income/expense totals, balance, monthly series, category breakdown, recent transactions")
    public DashboardSummary getSummary(@AuthenticationPrincipal UserPrincipal principal) {
        return dashboardService.getSummary(principal.getId());
    }
}