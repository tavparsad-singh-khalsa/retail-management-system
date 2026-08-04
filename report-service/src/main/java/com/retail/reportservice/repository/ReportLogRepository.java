package com.retail.reportservice.repository;

import com.retail.reportservice.entity.ReportLog;
import com.retail.reportservice.enums.ReportStatus;
import com.retail.reportservice.enums.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportLogRepository extends JpaRepository<ReportLog, Long> {

    Optional<ReportLog> findByReportNumber(String reportNumber);

    @Query(value = "SELECT nextval('report_number_seq')", nativeQuery = true)
    long nextReportNumberSequence();

    List<ReportLog> findByReportType(ReportType reportType);

    List<ReportLog> findByStatus(ReportStatus status);

    List<ReportLog> findByGeneratedAtBetween(LocalDateTime start, LocalDateTime end);

    List<ReportLog> findByReportTypeAndGeneratedAtBetween(ReportType reportType, LocalDateTime start, LocalDateTime end);

    boolean existsByReportNumber(String reportNumber);
}
