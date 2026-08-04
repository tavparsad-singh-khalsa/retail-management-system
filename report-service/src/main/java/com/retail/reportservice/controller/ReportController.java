package com.retail.reportservice.controller;

import com.retail.reportservice.dto.request.GenerateReportRequest;
import com.retail.reportservice.dto.response.*;
import com.retail.reportservice.enums.ReportType;
import com.retail.reportservice.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/sales")
    public ResponseEntity<SalesReportResponse> generateSalesReport(@Valid @RequestBody GenerateReportRequest request) {
        return ResponseEntity.ok(reportService.generateSalesReport(request));
    }

    @PostMapping("/purchase")
    public ResponseEntity<PurchaseReportResponse> generatePurchaseReport(@Valid @RequestBody GenerateReportRequest request) {
        return ResponseEntity.ok(reportService.generatePurchaseReport(request));
    }

    @PostMapping("/profit-loss")
    public ResponseEntity<ProfitLossReportResponse> generateProfitLossReport(@Valid @RequestBody GenerateReportRequest request) {
        return ResponseEntity.ok(reportService.generateProfitLossReport(request));
    }

    @PostMapping("/inventory")
    public ResponseEntity<InventoryStatusReportResponse> generateInventoryStatusReport(@Valid @RequestBody GenerateReportRequest request) {
        return ResponseEntity.ok(reportService.generateInventoryStatusReport(request));
    }

    @PostMapping("/dashboard")
    public ResponseEntity<ExecutiveDashboardResponse> generateExecutiveDashboard(@Valid @RequestBody GenerateReportRequest request) {
        return ResponseEntity.ok(reportService.generateExecutiveDashboard(request));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<ReportLogResponse>> getAllReportLogs() {
        return ResponseEntity.ok(reportService.getAllReportLogs());
    }

    @GetMapping("/logs/{id}")
    public ResponseEntity<ReportLogResponse> getReportLog(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportLog(id));
    }

    @GetMapping("/logs/number/{reportNumber}")
    public ResponseEntity<ReportLogResponse> getReportLogByNumber(@PathVariable String reportNumber) {
        return ResponseEntity.ok(reportService.getReportLogByNumber(reportNumber));
    }

    @GetMapping("/logs/type/{type}")
    public ResponseEntity<List<ReportLogResponse>> getReportLogsByType(@PathVariable ReportType type) {
        return ResponseEntity.ok(reportService.getReportLogsByType(type));
    }
}
