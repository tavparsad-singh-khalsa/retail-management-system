package com.retail.reportservice.dto.request;

import com.retail.reportservice.enums.ReportType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateReportRequest {

    @NotNull(message = "Report type is required")
    private ReportType reportType;

    private LocalDate startDate;

    private LocalDate endDate;

    private String generatedBy;
}
