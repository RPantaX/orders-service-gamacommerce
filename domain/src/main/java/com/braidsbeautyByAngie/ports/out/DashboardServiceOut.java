package com.braidsbeautyByAngie.ports.out;

import com.braidsbeautyByAngie.aggregates.dto.DashboardSummaryDTO;
import com.braidsbeautyByAngie.aggregates.dto.SalesAnalyticsDTO;
import com.braidsbeautyByAngie.aggregates.dto.TodayTransactionDTO;
import com.braidsbeautyByAngie.aggregates.dto.TopProductDTO;

import java.time.LocalDate;
import java.util.List;

public interface DashboardServiceOut {
    DashboardSummaryDTO getDashboardSummaryOut(Long companyId);
    List<SalesAnalyticsDTO> getSalesAnalyticsOut(String type, String period, LocalDate startDate, LocalDate endDate, Long companyId);
    List<TodayTransactionDTO> getTodayTransactionsOut(Long companyId);
    List<TopProductDTO> getTopProductsOut(String period, Long companyId);
}
