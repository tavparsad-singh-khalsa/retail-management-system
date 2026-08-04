package com.retail.reportservice.service;

import com.retail.reportservice.dto.request.GenerateReportRequest;
import com.retail.reportservice.dto.response.*;
import com.retail.reportservice.enums.ReportType;

import java.util.List;

public interface ReportService {

    SalesReportResponse generateSalesReport(GenerateReportRequest request);

    PurchaseReportResponse generatePurchaseReport(GenerateReportRequest request);

    ProfitLossReportResponse generateProfitLossReport(GenerateReportRequest request);

    InventoryStatusReportResponse generateInventoryStatusReport(GenerateReportRequest request);

    ExecutiveDashboardResponse generateExecutiveDashboard(GenerateReportRequest request);

    ReportLogResponse getReportLog(Long id);

    ReportLogResponse getReportLogByNumber(String reportNumber);

    List<ReportLogResponse> getAllReportLogs();

    List<ReportLogResponse> getReportLogsByType(ReportType reportType);
}
