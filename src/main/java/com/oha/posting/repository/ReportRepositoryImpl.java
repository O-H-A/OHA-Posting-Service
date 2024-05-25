package com.oha.posting.repository;

import com.oha.posting.entity.Report;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.oha.posting.entity.QReport.report;

@RequiredArgsConstructor
@Repository
public class ReportRepositoryImpl implements ReportRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Report> getReportList(BooleanBuilder builder) {

        return queryFactory
                .selectFrom(report)
                .join(report.post).fetchJoin()
                .join(report.reason).fetchJoin()
                .leftJoin(report.actions).fetchJoin()
                .where(builder)
                .orderBy(report.regDtm.asc())
                .fetch();
    }
}
