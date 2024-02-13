package com.oha.posting.service;

import com.oha.posting.config.exception.InvalidDataException;
import com.oha.posting.config.response.ResponseObject;
import com.oha.posting.config.response.StatusCode;
import com.oha.posting.dto.external.ExternalLocation;
import com.oha.posting.dto.request.WeatherInsertRequest;
import com.oha.posting.dto.request.WeatherUpdateRequest;
import com.oha.posting.dto.response.WeatherCountSearchResponse;
import com.oha.posting.dto.response.WeatherInsertResponse;
import com.oha.posting.entity.CommonCode;
import com.oha.posting.entity.Weather;
import com.oha.posting.repository.CommonCodeRepository;
import com.oha.posting.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class WeatherService {

    private final WeatherRepository weatherRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final ExternalApiService externalApiService;

    @Transactional(readOnly = true)
    public ResponseObject<List<WeatherCountSearchResponse>> getWeatherCount(String token, Long regionCode) {
        ResponseObject<List<WeatherCountSearchResponse>> response = new ResponseObject<>();
        try{
            int dayParts = getDayParts();
            Date currentDate = Date.valueOf(LocalDate.now());

            // 행정구역코드로 위치 조회 API 호출
            List<ExternalLocation> locationList = externalApiService.getLocationList(token, regionCode);
            // 주변 동네 포함
            List<Long> regionCodes = locationList.stream().map(item -> Long.parseLong(item.getCode())).toList();

            List<WeatherCountSearchResponse> data = weatherRepository.searchWeatherCount(regionCodes, dayParts, currentDate);

            response.setResponse(StatusCode.OK, "Success", data);
        } catch (InvalidDataException e) {
            log.warn("Exception during weather count search", e);
            response.setResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            log.warn("Exception during weather count search", e);
            response.setResponse(StatusCode.SERVER_ERROR, "조회에 실패하였습니다");
        }

        return response;
    }

    @Transactional
    public ResponseObject<WeatherInsertResponse> insertWeather(WeatherInsertRequest dto, String token, Long userId) {
        ResponseObject<WeatherInsertResponse> response = new ResponseObject<>();

        try{
            Weather weather = new Weather();
            int dayParts = getDayParts();
            Date currentDate = Date.valueOf(LocalDate.now());

            // 중복 조회 (같은 시간대 등록된 동네 날씨 확인)
            if (weatherRepository.findByUserIdAndDayPartsAndWeatherDt(userId, dayParts, currentDate)
                    .isPresent()) {
                throw new InvalidDataException(StatusCode.BAD_REQUEST, "날씨는 한 번만 등록하실 수 있습니다.");
            }

            // 날씨 공통코드 확인
            CommonCode commonCode = commonCodeRepository.findByCode(dto.getWeatherCode())
                    .orElseThrow(() ->  new InvalidDataException(StatusCode.BAD_REQUEST, "날씨 유형이 없습니다."));
            weather.setWeatherCommonCode(commonCode);

            // 사용자 자주 가는 지역 확인
            List<ExternalLocation> userLocationList = externalApiService.getUserLocationList(token);
            if(userLocationList.stream().noneMatch(item -> dto.getRegionCode().toString().equals(item.getCode()))) {
                throw new InvalidDataException(StatusCode.BAD_REQUEST, "자주 가는 지역이 아닙니다.");
            }

            weather.setRegionCode(dto.getRegionCode());

            // 현재 시간 구간
            weather.setDayParts(dayParts);

            // 오늘 날짜
            weather.setWeatherDt(currentDate);

            weather.setUserId(userId);

            // DB 저장
            Weather savedWeather = weatherRepository.save(weather);
            response.setResponse(StatusCode.CREATED, "Success", new WeatherInsertResponse(savedWeather.getWeatherId()));

        } catch (InvalidDataException e) {
            log.warn("Exception during weather insert", e);
            response.setResponse(e.getStatusCode(), e.getMessage());
        }
        catch (Exception e) {
            log.warn("Exception during weather insert", e);
            response.setResponse(StatusCode.SERVER_ERROR, "저장에 실패하였습니다");
        }

        return response;
    }

    private int getDayParts() {
        LocalTime now = LocalTime.now();
        LocalTime time_0 = LocalTime.of(0, 0, 0);
        LocalTime time_6 = LocalTime.of(6, 0, 0);
        LocalTime time_12 = LocalTime.of(12, 0, 0);
        LocalTime time_18 = LocalTime.of(18, 0, 0);

        if((now.equals(time_0) || now.isAfter(time_0)) && now.isBefore(time_6)) {
            return 1;
        } else if((now.equals(time_6) || now.isAfter(time_6)) && now.isBefore(time_12)) {
            return 2;
        } else if((now.equals(time_12) || now.isAfter(time_12)) && now.isBefore(time_18)) {
            return 3;
        } else {
            return 4;
        }
    }

    @Transactional
    public ResponseObject<?> updateWeather(WeatherUpdateRequest dto, Long userId, String token) {
        ResponseObject<?> response = new ResponseObject<>();

        try {
            // 동네 날씨 조회
            Weather weather = weatherRepository.findById(dto.getWeatherId()).orElseThrow(
                    () -> new InvalidDataException(StatusCode.BAD_REQUEST, "날씨 정보가 없습니다."));

            // 본인 확인
            if(!userId.equals(weather.getUserId())) {
                throw new InvalidDataException(StatusCode.FORBIDDEN, "권한이 없습니다.");
            }

            // 사용자 자주 가는 지역 확인
            List<ExternalLocation> userLocationList = externalApiService.getUserLocationList(token);
            if(userLocationList.stream().noneMatch(item -> dto.getRegionCode().toString().equals(item.getCode()))) {
                throw new InvalidDataException(StatusCode.BAD_REQUEST, "자주 가는 지역이 아닙니다.");
            }
            weather.setRegionCode(dto.getRegionCode());

            // 날씨 공통코드 확인
            CommonCode commonCode = commonCodeRepository.findByCode(dto.getWeatherCode())
                    .orElseThrow(() ->  new InvalidDataException(StatusCode.BAD_REQUEST, "날씨 유형이 없습니다."));
            weather.setWeatherCommonCode(commonCode);

            response.setResponse(StatusCode.OK, "Success");
        } catch (InvalidDataException e) {
            log.warn("Exception during weather update", e);
            response.setResponse(e.getStatusCode(), e.getMessage());
        }
        catch (Exception e) {
            log.warn("Exception during weather update", e);
            response.setResponse(StatusCode.SERVER_ERROR, "저장에 실패하였습니다");
        }

        return response;
    }

    public ResponseObject<?> deleteWeather(Long userId, Long weatherId) {
        ResponseObject<?> response = new ResponseObject<>();

        try{
            // 동네 날씨 조회
            Weather weather = weatherRepository.findById(weatherId).orElseThrow(
                    () -> new InvalidDataException(StatusCode.BAD_REQUEST, "날씨 정보가 없습니다."));

            // 본인 확인
            if(!userId.equals(weather.getUserId())) {
                throw new InvalidDataException(StatusCode.FORBIDDEN, "권한이 없습니다.");
            }

            // 삭제
            weatherRepository.delete(weather);
            response.setResponse(StatusCode.OK, "Success");
        } catch (InvalidDataException e) {
            log.warn("Exception during weather delete", e);
            response.setResponse(e.getStatusCode(), e.getMessage());
        }
        catch (Exception e) {
            log.warn("Exception during weather delete", e);
            response.setResponse(StatusCode.SERVER_ERROR, "삭제에 실패하였습니다");
        }

        return response;
    }
}
