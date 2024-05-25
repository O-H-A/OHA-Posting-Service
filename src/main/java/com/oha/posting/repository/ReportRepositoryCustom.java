package com.oha.posting.repository;

import com.oha.posting.entity.Report;
import com.querydsl.core.BooleanBuilder;

import java.util.List;

public interface ReportRepositoryCustom {

    List<Report> getReportList(BooleanBuilder builder);
}
