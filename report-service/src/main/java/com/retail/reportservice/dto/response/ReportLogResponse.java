package com.retail.reportservice.dto.response;

import com.retail.reportservice.enums.ReportStatus;
import com.retail.reportservice.enums.ReportType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportLogResponse {

    private Long id;
    private String reportNumber;
    private ReportType reportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String generatedBy;
    private ReportStatus status;
    private String reportUrl;
    private Long executionTimeMs;
    private LocalDateTime generatedAt;
    private String remarks;
}
