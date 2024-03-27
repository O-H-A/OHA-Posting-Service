package com.oha.posting.repository;

import com.oha.posting.dto.weather.WeatherCountSearchResponse;
import com.oha.posting.dto.weather.WeatherSearchResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

import static com.oha.posting.entity.QCommonCode.commonCode;
import static com.oha.posting.entity.QWeather.weather;

@RequiredArgsConstructor
@Repository
public class WeatherRepositoryImpl implements WeatherRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<WeatherCountSearchResponse> searchWeatherCount(List<Long> regionCodes, int dayParts, Date currentDate) {
        return queryFactory
                .select(Projections.constructor(WeatherCountSearchResponse.class
                        , commonCode.code
                        , commonCode.codeName
                        , weather.weatherCommonCode.count()
                        ))
                .from(commonCode)
                .leftJoin(weather)
                .on(commonCode.code.eq(weather.weatherCommonCode.code)
                        .and(weather.regionCode.in(regionCodes))
                        .and(weather.dayParts.eq(dayParts))
                        .and(weather.weatherDt.eq((currentDate)))
                )
                .where(commonCode.type.eq("WEATHER"))
                .groupBy(commonCode.code)
                .orderBy(weather.weatherCommonCode.count().desc())
                .fetch();
    }

    @Override
    public List<WeatherSearchResponse> searchWeather(Long userId, int dayParts, Date currentDate) {
        return queryFactory
                .select(Projections.constructor(WeatherSearchResponse.class
                        , weather.weatherId
                        , weather.userId
                        , weather.regionCode
                        , commonCode.code
                        , commonCode.codeName
                        , weather.dayParts
                        , Expressions.stringTemplate("TO_CHAR({0}, {1})",weather.weatherDt, "YYYY-MM-DD")
                ))
                .from(weather)
                .innerJoin(commonCode)
                .on(weather.weatherCommonCode.code.eq(commonCode.code))
                .where(weather.userId.eq(userId), weather.dayParts.eq(dayParts), weather.weatherDt.eq(currentDate))
                .fetch();
    }
}
