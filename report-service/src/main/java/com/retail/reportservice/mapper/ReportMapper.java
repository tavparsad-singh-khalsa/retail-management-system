package com.retail.reportservice.mapper;

import com.retail.reportservice.entity.ReportLog;
import com.retail.reportservice.dto.response.ReportLogResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ReportMapper {

    public ReportLogResponse toReportLogResponse(ReportLog entity) {
        if (entity == null) {
            return null;
        }

        return ReportLogResponse.builder()
                .id(entity.getId())
                .reportNumber(entity.getReportNumber())
                .reportType(entity.getReportType())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .generatedBy(entity.getGeneratedBy())
                .status(entity.getStatus())
                .reportUrl(entity.getReportUrl())
                .executionTimeMs(entity.getExecutionTimeMs())
                .generatedAt(entity.getGeneratedAt())
                .remarks(entity.getRemarks())
                .build();
    }

    public List<ReportLogResponse> toReportLogResponses(List<ReportLog> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toReportLogResponse)
                .toList();
    }
}
