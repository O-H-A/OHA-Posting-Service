package com.oha.posting.repository;

import com.oha.posting.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long>, ReportRepositoryCustom {
}
