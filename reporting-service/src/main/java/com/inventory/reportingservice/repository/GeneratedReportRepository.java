package com.inventory.reportingservice.repository;

import com.inventory.reportingservice.entity.GeneratedReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneratedReportRepository extends JpaRepository<GeneratedReport, Long> {
}
