package com.braidsbeautyByAngie.controller;

import com.braidsbeautyByAngie.ports.in.DashboardServiceIn;
import pe.com.gamacommerce.corelibraryservicegamacommerce.aggregates.aggregates.util.ApiResponse;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@OpenAPIDefinition(
        info = @Info(
                title = "API-DASHBOARD",
                version = "1.0",
                description = "Dashboard analytics management"
        )
)
@RestController
@RequestMapping("/v1/orders-service/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardServiceIn dashboardService;

    @Operation(summary = "Get dashboard summary cards")
    @GetMapping("/summary/company/{companyId}")
    public ResponseEntity<ApiResponse> getDashboardSummary(
            @PathVariable(name = "companyId") Long companyId) {
        return ResponseEntity.ok(ApiResponse.ok("Dashboard summary retrieved successfully",
                dashboardService.getDashboardSummaryIn(companyId)));
    }

    @Operation(summary = "Get sales analytics chart data")
    @GetMapping("/analytics/company/{companyId}")
    public ResponseEntity<ApiResponse> getSalesAnalytics(
            @PathVariable(name = "companyId") Long companyId,
            @RequestParam(defaultValue = "PRODUCT") String type,
            @RequestParam(defaultValue = "MONTHLY") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.ok("Sales analytics data retrieved successfully",
                dashboardService.getSalesAnalyticsIn(type, period, startDate, endDate, companyId)));
    }

    @Operation(summary = "Get today's transactions")
    @GetMapping("/transactions/today/company/{companyId}")
    public ResponseEntity<ApiResponse> getTodayTransactions(
            @PathVariable(name = "companyId") Long companyId) {
        return ResponseEntity.ok(ApiResponse.ok("Today's transactions retrieved successfully",
                dashboardService.getTodayTransactionsIn(companyId)));
    }

    @Operation(summary = "Get top selling products")
    @GetMapping("/top-products/company/{companyId}")
    public ResponseEntity<ApiResponse> getTopProducts(
            @PathVariable(name = "companyId") Long companyId,
            @RequestParam(defaultValue = "MONTHLY") String period) {
        return ResponseEntity.ok(ApiResponse.ok("Top products retrieved successfully",
                dashboardService.getTopProductsIn(period, companyId)));
    }
}