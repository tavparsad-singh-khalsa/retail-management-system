package com.retail.reportservice.service.impl;

import com.retail.reportservice.client.*;
import com.retail.reportservice.dto.external.*;
import com.retail.reportservice.dto.request.GenerateReportRequest;
import com.retail.reportservice.dto.response.*;
import com.retail.reportservice.entity.ReportLog;
import com.retail.reportservice.enums.ReportStatus;
import com.retail.reportservice.enums.ReportType;
import com.retail.reportservice.exception.InvalidReportRequestException;
import com.retail.reportservice.exception.ReportNotFoundException;
import com.retail.reportservice.mapper.ReportMapper;
import com.retail.reportservice.repository.ReportLogRepository;
import com.retail.reportservice.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportLogRepository reportLogRepository;
    private final ReportMapper reportMapper;

    private final SalesClient salesClient;
    private final PurchaseClient purchaseClient;
    private final BillingClient billingClient;
    private final ProductClient productClient;
    private final CustomerClient customerClient;
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public SalesReportResponse generateSalesReport(GenerateReportRequest request) {
        log.info("Generating Sales Report...");
        validateDates(request.getStartDate(), request.getEndDate());

        long startTime = System.currentTimeMillis();
        ReportLog logEntity = createInitialReportLog(ReportType.SALES_SUMMARY, request);

        try {
            List<ExternalSaleDto> sales = salesClient.getSalesByDateRange(request.getStartDate(), request.getEndDate());

            long totalSalesCount = sales.size();
            BigDecimal totalRevenue = sales.stream()
                    .map(s -> s.getTotalAmount() != null ? s.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalTax = sales.stream()
                    .map(s -> s.getTaxAmount() != null ? s.getTaxAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalDiscount = sales.stream()
                    .map(s -> s.getDiscountAmount() != null ? s.getDiscountAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal avgOrderValue = totalSalesCount > 0
                    ? totalRevenue.divide(BigDecimal.valueOf(totalSalesCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            List<SalesReportResponse.TopProductSummary> topProducts = calculateTopSellingProducts(sales);

            completeReportLog(logEntity, startTime, "Successfully generated Sales Report. Total revenue: " + totalRevenue);

            return SalesReportResponse.builder()
                    .reportNumber(logEntity.getReportNumber())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .totalSalesCount(totalSalesCount)
                    .totalRevenue(totalRevenue)
                    .totalTaxAmount(totalTax)
                    .totalDiscountAmount(totalDiscount)
                    .averageOrderValue(avgOrderValue)
                    .topSellingProducts(topProducts)
                    .generatedAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            failReportLog(logEntity, startTime, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public PurchaseReportResponse generatePurchaseReport(GenerateReportRequest request) {
        log.info("Generating Purchase Report...");
        validateDates(request.getStartDate(), request.getEndDate());

        long startTime = System.currentTimeMillis();
        ReportLog logEntity = createInitialReportLog(ReportType.PURCHASE_SUMMARY, request);

        try {
            List<ExternalPurchaseDto> purchases = purchaseClient.getPurchasesByDateRange(request.getStartDate(), request.getEndDate());

            long totalPurchasesCount = purchases.size();
            BigDecimal totalCost = purchases.stream()
                    .map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal avgPurchaseValue = totalPurchasesCount > 0
                    ? totalCost.divide(BigDecimal.valueOf(totalPurchasesCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            completeReportLog(logEntity, startTime, "Successfully generated Purchase Report. Total cost: " + totalCost);

            return PurchaseReportResponse.builder()
                    .reportNumber(logEntity.getReportNumber())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .totalPurchasesCount(totalPurchasesCount)
                    .totalPurchaseCost(totalCost)
                    .averagePurchaseValue(avgPurchaseValue)
                    .generatedAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            failReportLog(logEntity, startTime, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public ProfitLossReportResponse generateProfitLossReport(GenerateReportRequest request) {
        log.info("Generating Profit & Loss Report...");
        validateDates(request.getStartDate(), request.getEndDate());

        long startTime = System.currentTimeMillis();
        ReportLog logEntity = createInitialReportLog(ReportType.PROFIT_LOSS, request);

        try {
            List<ExternalSaleDto> sales = salesClient.getSalesByDateRange(request.getStartDate(), request.getEndDate());
            List<ExternalPurchaseDto> purchases = purchaseClient.getPurchasesByDateRange(request.getStartDate(), request.getEndDate());

            BigDecimal totalRevenue = sales.stream()
                    .map(s -> s.getTotalAmount() != null ? s.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCostOfGoods = purchases.stream()
                    .map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal grossProfit = totalRevenue.subtract(totalCostOfGoods);

            BigDecimal profitMarginPercentage = BigDecimal.ZERO;
            if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                profitMarginPercentage = grossProfit.multiply(BigDecimal.valueOf(100))
                        .divide(totalRevenue, 2, RoundingMode.HALF_UP);
            }

            completeReportLog(logEntity, startTime, "Successfully generated Profit & Loss Report. Gross profit: " + grossProfit);

            return ProfitLossReportResponse.builder()
                    .reportNumber(logEntity.getReportNumber())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .totalRevenue(totalRevenue)
                    .totalCostOfGoodsPurchased(totalCostOfGoods)
                    .grossProfit(grossProfit)
                    .profitMarginPercentage(profitMarginPercentage)
                    .generatedAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            failReportLog(logEntity, startTime, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public InventoryStatusReportResponse generateInventoryStatusReport(GenerateReportRequest request) {
        log.info("Generating Inventory Status Report...");

        long startTime = System.currentTimeMillis();
        ReportLog logEntity = createInitialReportLog(ReportType.INVENTORY_STATUS, request);

        try {
            List<ExternalProductDto> products = productClient.getAllProducts();

            long totalProductsCount = products.size();
            long inStockCount = products.stream()
                    .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() > (p.getMinStockLevel() != null ? p.getMinStockLevel() : 5))
                    .count();

            long lowStockCount = products.stream()
                    .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() > 0 && p.getStockQuantity() <= (p.getMinStockLevel() != null ? p.getMinStockLevel() : 5))
                    .count();

            long outOfStockCount = products.stream()
                    .filter(p -> p.getStockQuantity() == null || p.getStockQuantity() <= 0)
                    .count();

            List<InventoryStatusReportResponse.LowStockItemSummary> lowStockItems = products.stream()
                    .filter(p -> p.getStockQuantity() == null || p.getStockQuantity() <= (p.getMinStockLevel() != null ? p.getMinStockLevel() : 5))
                    .map(p -> InventoryStatusReportResponse.LowStockItemSummary.builder()
                            .productId(p.getId())
                            .sku(p.getSku())
                            .name(p.getName())
                            .currentStock(p.getStockQuantity() != null ? p.getStockQuantity() : 0)
                            .minStockLevel(p.getMinStockLevel() != null ? p.getMinStockLevel() : 5)
                            .build())
                    .toList();

            completeReportLog(logEntity, startTime, "Successfully generated Inventory Status Report. Low/Out of stock items: " + (lowStockCount + outOfStockCount));

            return InventoryStatusReportResponse.builder()
                    .reportNumber(logEntity.getReportNumber())
                    .totalProductsCount(totalProductsCount)
                    .inStockCount(inStockCount)
                    .lowStockCount(lowStockCount)
                    .outOfStockCount(outOfStockCount)
                    .lowStockItems(lowStockItems)
                    .generatedAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            failReportLog(logEntity, startTime, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public ExecutiveDashboardResponse generateExecutiveDashboard(GenerateReportRequest request) {
        log.info("Generating Executive Dashboard Report...");

        long startTime = System.currentTimeMillis();
        ReportLog logEntity = createInitialReportLog(ReportType.EXECUTIVE_DASHBOARD, request);

        try {
            List<ExternalCustomerDto> customers = customerClient.getAllCustomers();
            List<ExternalProductDto> products = productClient.getAllProducts();
            List<ExternalSaleDto> sales = salesClient.getAllSales();
            List<ExternalPurchaseDto> purchases = purchaseClient.getAllPurchases();
            List<ExternalInvoiceDto> invoices = billingClient.getAllInvoices();
            ExternalNotificationCountsDto notifCounts = notificationClient.getDashboardCounts();

            long totalCustomers = customers.size();
            long totalProducts = products.size();

            long totalSalesCount = sales.size();
            BigDecimal totalRevenue = sales.stream()
                    .map(s -> s.getTotalAmount() != null ? s.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long totalPurchasesCount = purchases.size();
            BigDecimal totalPurchaseCost = purchases.stream()
                    .map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long pendingInvoicesCount = invoices.stream()
                    .filter(i -> "GENERATED".equalsIgnoreCase(i.getInvoiceStatus()) || "PENDING".equalsIgnoreCase(i.getPaymentStatus()))
                    .count();

            long pendingNotifications = notifCounts.getPending();

            completeReportLog(logEntity, startTime, "Successfully generated Executive Dashboard.");

            return ExecutiveDashboardResponse.builder()
                    .reportNumber(logEntity.getReportNumber())
                    .totalCustomers(totalCustomers)
                    .totalProducts(totalProducts)
                    .totalSalesCount(totalSalesCount)
                    .totalRevenue(totalRevenue)
                    .totalPurchasesCount(totalPurchasesCount)
                    .totalPurchaseCost(totalPurchaseCost)
                    .pendingInvoicesCount(pendingInvoicesCount)
                    .pendingNotificationsCount(pendingNotifications)
                    .generatedAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            failReportLog(logEntity, startTime, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReportLogResponse getReportLog(Long id) {
        ReportLog reportLog = reportLogRepository.findById(id)
                .orElseThrow(() -> new ReportNotFoundException("Report log not found with ID: " + id));
        return reportMapper.toReportLogResponse(reportLog);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportLogResponse getReportLogByNumber(String reportNumber) {
        ReportLog reportLog = reportLogRepository.findByReportNumber(reportNumber)
                .orElseThrow(() -> new ReportNotFoundException("Report log not found with number: " + reportNumber));
        return reportMapper.toReportLogResponse(reportLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportLogResponse> getAllReportLogs() {
        return reportMapper.toReportLogResponses(reportLogRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportLogResponse> getReportLogsByType(ReportType reportType) {
        return reportMapper.toReportLogResponses(reportLogRepository.findByReportType(reportType));
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidReportRequestException("Start date (" + startDate + ") cannot be after end date (" + endDate + ")");
        }
    }

    private ReportLog createInitialReportLog(ReportType reportType, GenerateReportRequest request) {
        String reportNumber = generateReportNumber();
        ReportLog logEntity = ReportLog.builder()
                .reportNumber(reportNumber)
                .reportType(reportType)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .generatedBy(request.getGeneratedBy() != null ? request.getGeneratedBy() : "SYSTEM")
                .status(ReportStatus.IN_PROGRESS)
                .reportUrl("/reports/" + reportNumber + ".json")
                .build();
        return reportLogRepository.save(logEntity);
    }

    private void completeReportLog(ReportLog logEntity, long startTime, String remarks) {
        long executionTimeMs = System.currentTimeMillis() - startTime;
        logEntity.setStatus(ReportStatus.SUCCESS);
        logEntity.setExecutionTimeMs(executionTimeMs);
        logEntity.setRemarks(remarks);
        reportLogRepository.save(logEntity);
    }

    private void failReportLog(ReportLog logEntity, long startTime, String errorMessage) {
        long executionTimeMs = System.currentTimeMillis() - startTime;
        logEntity.setStatus(ReportStatus.FAILED);
        logEntity.setExecutionTimeMs(executionTimeMs);
        logEntity.setRemarks("Failed: " + errorMessage);
        reportLogRepository.save(logEntity);
    }

    private String generateReportNumber() {
        long sequence = reportLogRepository.nextReportNumberSequence();
        int year = java.time.Year.now().getValue();
        return String.format("REP-%d-%06d", year, sequence);
    }

    private List<SalesReportResponse.TopProductSummary> calculateTopSellingProducts(List<ExternalSaleDto> sales) {
        Map<Long, SalesReportResponse.TopProductSummary> productMap = new HashMap<>();

        for (ExternalSaleDto sale : sales) {
            if (sale.getSaleItems() == null) continue;
            for (ExternalSaleDto.ExternalSaleItemDto item : sale.getSaleItems()) {
                if (item.getProductVariantId() == null) continue;

                Long pId = item.getProductVariantId();
                long qty = item.getQuantity() != null ? item.getQuantity() : 0;
                BigDecimal lineTot = item.getTotalAmount() != null ? item.getTotalAmount() : BigDecimal.ZERO;
                String pName = "Product #" + pId;

                productMap.compute(pId, (id, existing) -> {
                    if (existing == null) {
                        return SalesReportResponse.TopProductSummary.builder()
                                .productId(pId)
                                .productName(pName)
                                .quantitySold(qty)
                                .totalRevenue(lineTot)
                                .build();
                    } else {
                        existing.setQuantitySold(existing.getQuantitySold() + qty);
                        existing.setTotalRevenue(existing.getTotalRevenue().add(lineTot));
                        return existing;
                    }
                });
            }
        }

        return productMap.values().stream()
                .sorted(Comparator.comparing(SalesReportResponse.TopProductSummary::getQuantitySold).reversed())
                .limit(10)
                .toList();
    }
}
