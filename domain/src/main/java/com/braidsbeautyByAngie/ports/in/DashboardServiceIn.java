package com.braidsbeautyByAngie.ports.in;

import com.braidsbeautyByAngie.aggregates.dto.DashboardSummaryDTO;
import com.braidsbeautyByAngie.aggregates.dto.SalesAnalyticsDTO;
import com.braidsbeautyByAngie.aggregates.dto.TodayTransactionDTO;
import com.braidsbeautyByAngie.aggregates.dto.TopProductDTO;

import java.time.LocalDate;
import java.util.List;

public interface DashboardServiceIn {
    DashboardSummaryDTO getDashboardSummaryIn(Long companyId);
    List<SalesAnalyticsDTO> getSalesAnalyticsIn(String type, String period, LocalDate startDate, LocalDate endDate, Long companyId);
    List<TodayTransactionDTO> getTodayTransactionsIn(Long companyId);
    List<TopProductDTO> getTopProductsIn(String period, Long companyId);
}
