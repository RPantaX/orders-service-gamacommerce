package com.braidsbeautyByAngie.adapters;

import com.braidsbeautyByAngie.aggregates.dto.DashboardSummaryDTO;
import com.braidsbeautyByAngie.aggregates.dto.SalesAnalyticsDTO;
import com.braidsbeautyByAngie.aggregates.dto.TodayTransactionDTO;
import com.braidsbeautyByAngie.aggregates.dto.TopProductDTO;
import com.braidsbeautyByAngie.aggregates.response.rest.products.ResponseProductItemDetail;
import com.braidsbeautyByAngie.aggregates.types.ShopOrderStatusEnum;
import com.braidsbeautyByAngie.entity.ShopOrderEntity;
import com.braidsbeautyByAngie.ports.out.DashboardServiceOut;
import com.braidsbeautyByAngie.repository.OrderLineRepository;
import com.braidsbeautyByAngie.repository.ShopOrderRepository;
import com.braidsbeautyByAngie.rest.RestProductsAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceAdapter implements DashboardServiceOut {

    private final ShopOrderRepository shopOrderRepository;
    private final OrderLineRepository orderLineRepository;
    private final RestProductsAdapter restProductsAdapter;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public DashboardSummaryDTO getDashboardSummaryOut(Long companyId) {
        log.info("Generating dashboard summary for company: {}", companyId);

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        // Total sales this month
        BigDecimal totalSales = calculateTotalSales(startOfMonth, endOfMonth, companyId);

        // In-store orders count (assuming shipping method ID 1 is in-store)
        Integer inStoreOrders = countInStoreOrders(startOfMonth, endOfMonth, companyId);

        // Online orders percentage
        Double onlinePercentage = calculateOnlineOrdersPercentage(startOfMonth, endOfMonth, companyId);

        // Star products count (products sold more than average)
        Integer starProducts = countStarProducts(companyId);

        return DashboardSummaryDTO.builder()
                .totalSalesThisMonth(totalSales)
                .inStoreOrdersCount(inStoreOrders)
                .onlineOrdersPercentage(onlinePercentage)
                .starProductsCount(starProducts)
                .build();
    }

    @Override
    public List<SalesAnalyticsDTO> getSalesAnalyticsOut(String type, String period, LocalDate startDate, LocalDate endDate, Long companyId) {
        log.info("Generating sales analytics for type: {}, period: {}, company: {}", type, period, companyId);

        List<SalesAnalyticsDTO> analytics = new ArrayList<>();

        if (startDate != null && endDate != null) {
            // Custom date range
            analytics = generateAnalyticsForDateRange(type, startDate, endDate, companyId);
        } else {
            switch (period.toUpperCase()) {
                case "WEEKLY":
                    analytics = generateWeeklyAnalytics(type, companyId);
                    break;
                case "MONTHLY":
                    analytics = generateMonthlyAnalytics(type, companyId);
                    break;
                case "YEARLY":
                    analytics = generateYearlyAnalytics(type, companyId);
                    break;
                default:
                    analytics = generateMonthlyAnalytics(type, companyId);
            }
        }

        return analytics;
    }

    @Override
    public List<TodayTransactionDTO> getTodayTransactionsOut(Long companyId) {
        log.info("Fetching today's transactions for company: {}", companyId);

        LocalDateTime startOfDay = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        String jpql = """
            SELECT so FROM ShopOrderEntity so 
            WHERE so.shopOrderDate BETWEEN :startOfDay AND :endOfDay 
            AND so.companyId = :companyId 
            ORDER BY so.shopOrderDate DESC
            """;

        List<ShopOrderEntity> todayOrders = entityManager.createQuery(jpql, ShopOrderEntity.class)
                .setParameter("startOfDay", java.sql.Timestamp.valueOf(startOfDay))
                .setParameter("endOfDay", java.sql.Timestamp.valueOf(endOfDay))
                .setParameter("companyId", companyId)
                .getResultList();

        return todayOrders.stream()
                .map(this::mapToTodayTransactionDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TopProductDTO> getTopProductsOut(String period, Long companyId) {
        log.info("Fetching top products for period: {}, company: {}", period, companyId);

        LocalDate startDate = calculateStartDateByPeriod(period);
        LocalDate endDate = LocalDate.now();

        String nativeQuery = """
            SELECT 
                ol.product_item_id,
                SUM(ol.order_line_quantity) as total_sold
            FROM order_line ol
            INNER JOIN shop_order so ON ol.shop_order_id = so.shop_order_id
            WHERE ol.product_item_id IS NOT NULL
            AND so.shop_order_date BETWEEN :startDate AND :endDate
            AND so.shop_order_status = 'APPROVED'
            AND so.company_id = :companyId
            GROUP BY ol.product_item_id
            ORDER BY total_sold DESC
            LIMIT 10
            """;

        Query query = entityManager.createNativeQuery(nativeQuery);
        query.setParameter("startDate", java.sql.Date.valueOf(startDate));
        query.setParameter("endDate", java.sql.Date.valueOf(endDate));
        query.setParameter("companyId", companyId);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> {
                    Long productItemId = ((Number) row[0]).longValue();
                    Long totalSold = ((Number) row[1]).longValue();

                    try {
                        // Fetch product details from product service
                        List<ResponseProductItemDetail> productDetails = (List<ResponseProductItemDetail>)restProductsAdapter.listItemProductsByIds(List.of(productItemId)).getData();
                        var productDetail = productDetails
                                .stream().findFirst().orElse(null);

                        if (productDetail != null) {
                            return TopProductDTO.builder()
                                    .productImage(productDetail.getProductItemImage())
                                    .productName(productDetail.getProductItemSKU())
                                    .category(productDetail.getResponseCategory() != null ?
                                            productDetail.getResponseCategory().getProductCategoryName() : "Sin categoría")
                                    .price(productDetail.getProductItemPrice())
                                    .totalSold(totalSold)
                                    .build();
                        }
                    } catch (Exception e) {
                        log.error("Error fetching product details for ID: {}", productItemId, e);
                    }

                    return TopProductDTO.builder()
                            .productImage("default-image.jpg")
                            .productName("Producto #" + productItemId)
                            .category("Sin categoría")
                            .price(BigDecimal.ZERO)
                            .totalSold(totalSold)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // Private helper methods
    private BigDecimal calculateTotalSales(LocalDate startDate, LocalDate endDate, Long companyId) {
        String jpql = """
            SELECT COALESCE(SUM(so.shopOrderTotal), 0) FROM ShopOrderEntity so 
            WHERE DATE(so.shopOrderDate) BETWEEN :startDate AND :endDate 
            AND so.shopOrderStatus = :status
            AND so.companyId = :companyId
            """;

        Number result = (Number) entityManager.createQuery(jpql)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("status", ShopOrderStatusEnum.APPROVED)
                .setParameter("companyId", companyId)
                .getSingleResult();

        return result != null ? new BigDecimal(result.toString()) : BigDecimal.ZERO;
    }

    private Integer countInStoreOrders(LocalDate startDate, LocalDate endDate, Long companyId) {
        String jpql = """
            SELECT COUNT(so) FROM ShopOrderEntity so 
            WHERE DATE(so.shopOrderDate) BETWEEN :startDate AND :endDate 
            AND so.shoppingMethodEntity.shoppingMethodId = 1
            AND so.companyId = :companyId
            """;

        Number result = (Number) entityManager.createQuery(jpql)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("companyId", companyId)
                .getSingleResult();

        return result != null ? result.intValue() : 0;
    }

    private Double calculateOnlineOrdersPercentage(LocalDate startDate, LocalDate endDate, Long companyId) {
        String totalOrdersJpql = """
            SELECT COUNT(so) FROM ShopOrderEntity so 
            WHERE DATE(so.shopOrderDate) BETWEEN :startDate AND :endDate
            AND so.companyId = :companyId
            """;

        String onlineOrdersJpql = """
            SELECT COUNT(so) FROM ShopOrderEntity so 
            WHERE DATE(so.shopOrderDate) BETWEEN :startDate AND :endDate 
            AND so.shoppingMethodEntity.shoppingMethodId != 1
            AND so.companyId = :companyId
            """;

        Number totalOrders = (Number) entityManager.createQuery(totalOrdersJpql)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("companyId", companyId)
                .getSingleResult();

        Number onlineOrders = (Number) entityManager.createQuery(onlineOrdersJpql)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("companyId", companyId)
                .getSingleResult();

        if (totalOrders.intValue() == 0) return 0.0;

        return (onlineOrders.doubleValue() / totalOrders.doubleValue()) * 100;
    }

    private Integer countStarProducts(Long companyId) {
        // Products that sold more than average for this company
        String avgQuery = """
            SELECT AVG(total_sold) FROM (
                SELECT SUM(ol.order_line_quantity) as total_sold
                FROM order_line ol
                INNER JOIN shop_order so ON ol.shop_order_id = so.shop_order_id
                WHERE ol.product_item_id IS NOT NULL
                AND so.company_id = :companyId
                GROUP BY ol.product_item_id
            ) as subquery
            """;

        Number avgSold = (Number) entityManager.createNativeQuery(avgQuery)
                .setParameter("companyId", companyId)
                .getSingleResult();

        if (avgSold == null) return 0;

        String countQuery = """
            SELECT COUNT(*) FROM (
                SELECT ol.product_item_id
                FROM order_line ol
                INNER JOIN shop_order so ON ol.shop_order_id = so.shop_order_id
                WHERE ol.product_item_id IS NOT NULL
                AND so.company_id = :companyId
                GROUP BY ol.product_item_id
                HAVING SUM(ol.order_line_quantity) > :avgSold
            ) as star_products
            """;

        Number result = (Number) entityManager.createNativeQuery(countQuery)
                .setParameter("companyId", companyId)
                .setParameter("avgSold", avgSold)
                .getSingleResult();

        return result != null ? result.intValue() : 0;
    }

    private List<SalesAnalyticsDTO> generateMonthlyAnalytics(String type, Long companyId) {
        List<SalesAnalyticsDTO> analytics = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 11; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.from(now.minusMonths(i));
            LocalDate startOfMonth = yearMonth.atDay(1);
            LocalDate endOfMonth = yearMonth.atEndOfMonth();

            SalesAnalyticsDTO dto = calculateAnalyticsForPeriod(type, startOfMonth, endOfMonth, yearMonth.getMonth().name().substring(0, 3), companyId);
            analytics.add(dto);
        }

        return analytics;
    }

    private List<SalesAnalyticsDTO> generateWeeklyAnalytics(String type, Long companyId) {
        List<SalesAnalyticsDTO> analytics = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 11; i >= 0; i--) {
            LocalDate weekStart = now.minusWeeks(i).minusDays(now.minusWeeks(i).getDayOfWeek().getValue() - 1);
            LocalDate weekEnd = weekStart.plusDays(6);

            SalesAnalyticsDTO dto = calculateAnalyticsForPeriod(type, weekStart, weekEnd, "Week " + (12 - i), companyId);
            analytics.add(dto);
        }

        return analytics;
    }

    private List<SalesAnalyticsDTO> generateYearlyAnalytics(String type, Long companyId) {
        List<SalesAnalyticsDTO> analytics = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 4; i >= 0; i--) {
            LocalDate yearStart = LocalDate.of(now.getYear() - i, 1, 1);
            LocalDate yearEnd = LocalDate.of(now.getYear() - i, 12, 31);

            SalesAnalyticsDTO dto = calculateAnalyticsForPeriod(type, yearStart, yearEnd, String.valueOf(now.getYear() - i), companyId);
            analytics.add(dto);
        }

        return analytics;
    }

    private List<SalesAnalyticsDTO> generateAnalyticsForDateRange(String type, LocalDate startDate, LocalDate endDate, Long companyId) {
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        List<SalesAnalyticsDTO> analytics = new ArrayList<>();

        if (daysBetween <= 31) {
            // Daily breakdown
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                SalesAnalyticsDTO dto = calculateAnalyticsForPeriod(type, current, current, current.toString(), companyId);
                analytics.add(dto);
                current = current.plusDays(1);
            }
        } else {
            // Monthly breakdown
            YearMonth start = YearMonth.from(startDate);
            YearMonth end = YearMonth.from(endDate);

            YearMonth current = start;
            while (!current.isAfter(end)) {
                LocalDate monthStart = current.atDay(1);
                LocalDate monthEnd = current.atEndOfMonth();

                if (monthStart.isBefore(startDate)) monthStart = startDate;
                if (monthEnd.isAfter(endDate)) monthEnd = endDate;

                SalesAnalyticsDTO dto = calculateAnalyticsForPeriod(type, monthStart, monthEnd, current.getMonth().name().substring(0, 3), companyId);
                analytics.add(dto);
                current = current.plusMonths(1);
            }
        }

        return analytics;
    }

    private SalesAnalyticsDTO calculateAnalyticsForPeriod(String type, LocalDate startDate, LocalDate endDate, String periodName, Long companyId) {
        String productOrdersQuery = """
            SELECT COUNT(DISTINCT so.shop_order_id) FROM shop_order so
            INNER JOIN order_line ol ON so.shop_order_id = ol.shop_order_id
            WHERE DATE(so.shop_order_date) BETWEEN :startDate AND :endDate
            AND ol.product_item_id IS NOT NULL
            AND so.company_id = :companyId
            """;

        String serviceOrdersQuery = """
            SELECT COUNT(DISTINCT so.shop_order_id) FROM shop_order so
            INNER JOIN order_line ol ON so.shop_order_id = ol.shop_order_id
            WHERE DATE(so.shop_order_date) BETWEEN :startDate AND :endDate
            AND so.company_id = :companyId
            """;

        String totalOrdersQuery = """
            SELECT COUNT(so.shop_order_id) FROM shop_order so
            WHERE DATE(so.shop_order_date) BETWEEN :startDate AND :endDate
            AND so.company_id = :companyId
            """;

        Number productOrders = (Number) entityManager.createNativeQuery(productOrdersQuery)
                .setParameter("startDate", java.sql.Date.valueOf(startDate))
                .setParameter("endDate", java.sql.Date.valueOf(endDate))
                .setParameter("companyId", companyId)
                .getSingleResult();

        Number serviceOrders = (Number) entityManager.createNativeQuery(serviceOrdersQuery)
                .setParameter("startDate", java.sql.Date.valueOf(startDate))
                .setParameter("endDate", java.sql.Date.valueOf(endDate))
                .setParameter("companyId", companyId)
                .getSingleResult();

        Number totalOrders = (Number) entityManager.createNativeQuery(totalOrdersQuery)
                .setParameter("startDate", java.sql.Date.valueOf(startDate))
                .setParameter("endDate", java.sql.Date.valueOf(endDate))
                .setParameter("companyId", companyId)
                .getSingleResult();

        return SalesAnalyticsDTO.builder()
                .period(periodName)
                .productOrders(productOrders != null ? productOrders.longValue() : 0L)
                .serviceOrders(serviceOrders != null ? serviceOrders.longValue() : 0L)
                .totalOrders(totalOrders != null ? totalOrders.longValue() : 0L)
                .build();
    }

    private TodayTransactionDTO mapToTodayTransactionDTO(ShopOrderEntity order) {
        String orderType = determineOrderType(order);
        String customerName = "Customer #" + order.getUserId();

        return TodayTransactionDTO.builder()
                .orderId("#" + order.getShopOrderId())
                .customerName(customerName)
                .orderType(orderType)
                .orderDate(order.getShopOrderDate().toLocalDateTime())
                .status(order.getShopOrderStatus().name())
                .amount(order.getShopOrderTotal() != null ? order.getShopOrderTotal() : BigDecimal.ZERO)
                .build();
    }

    private String determineOrderType(ShopOrderEntity order) {
        boolean hasProducts = order.getOrderLineEntities().stream()
                .anyMatch(ol -> ol.getProductItemId() != null);
        if (hasProducts) return "Product";
        return "Unknown";
    }

    private LocalDate calculateStartDateByPeriod(String period) {
        LocalDate now = LocalDate.now();

        return switch (period.toUpperCase()) {
            case "WEEKLY" -> now.minusWeeks(1);
            case "MONTHLY" -> now.minusMonths(1);
            case "YEARLY" -> now.minusYears(1);
            default -> now.minusMonths(1);
        };
    }
}