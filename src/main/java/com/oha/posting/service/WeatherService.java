package com.oha.posting.service;

import com.oha.posting.config.exception.InvalidDataException;
import com.oha.posting.config.response.ResponseObject;
import com.oha.posting.dto.external.ExternalLocation;
import com.oha.posting.dto.kafka.WeatherRegEvent;
import com.oha.posting.dto.weather.WeatherInsertRequest;
import com.oha.posting.dto.weather.WeatherUpdateRequest;
import com.oha.posting.dto.weather.WeatherCountSearchResponse;
import com.oha.posting.dto.weather.WeatherInsertResponse;
import com.oha.posting.dto.weather.WeatherSearchResponse;
import com.oha.posting.entity.CommonCode;
import com.oha.posting.entity.Weather;
import com.oha.posting.repository.CommonCodeRepository;
import com.oha.posting.repository.WeatherRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Service
public class WeatherService {

    private final WeatherRepository weatherRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final ExternalApiService externalApiService;
    private final KafkaProducer kafkaProducer;

    @Transactional(readOnly = true)
    public ResponseObject<List<WeatherCountSearchResponse>> getWeatherCount(String token, Long regionCode) throws Exception {
        ResponseObject<List<WeatherCountSearchResponse>> response = new ResponseObject<>();
        try{
            int dayParts = getDayParts();
            Date currentDate = Date.valueOf(LocalDate.now());

            // 행정구역코드로 위치 조회 API 호출
            Map<String, ExternalLocation> locationMap = externalApiService.getLocationMap(token, regionCode);

            // 주변 동네 포함
            List<Long> regionCodes = (locationMap.keySet()).stream().map(item -> Long.parseLong(item)).toList();

            List<WeatherCountSearchResponse> data = weatherRepository.searchWeatherCount(regionCodes, dayParts, currentDate);

            response.setResponse(HttpStatus.OK.value(), "Success", data);
        } catch (InvalidDataException e) {
            log.warn("Exception during weather count search", e);
            throw e;
        } catch (Exception e) {
            log.warn("Exception during weather count search", e);
            throw new Exception("동네 날씨 조회에 실패하였습니다");
        }

        return response;
    }

    @Transactional(rollbackFor = {Exception.class})
    public ResponseObject<WeatherInsertResponse> insertWeather(WeatherInsertRequest dto, String token, Long userId, HttpServletResponse httpServletResponse) throws Exception {
        ResponseObject<WeatherInsertResponse> response = new ResponseObject<>();

        try{
            Weather weather = new Weather();
            int dayParts = getDayParts();
            Date currentDate = Date.valueOf(LocalDate.now());

            // 중복 조회 (같은 시간대 등록된 동네 날씨 확인)
            if (weatherRepository.findByUserIdAndDayPartsAndWeatherDt(userId, dayParts, currentDate)
                    .isPresent()) {
                throw new InvalidDataException(HttpStatus.CONFLICT, "날씨는 한 번만 등록하실 수 있습니다.");
            }

            // 날씨 공통코드 확인
            CommonCode commonCode = commonCodeRepository.findByCode(dto.getWeatherCode())
                    .orElseThrow(() ->  new InvalidDataException(HttpStatus.BAD_REQUEST, "날씨 유형이 없습니다."));
            weather.setWeatherCommonCode(commonCode);

            // 사용자 자주 가는 지역 확인
            List<ExternalLocation> userLocationList = externalApiService.getUserLocationList(token);
            if(userLocationList.stream().noneMatch(item -> dto.getRegionCode().toString().equals(item.getCode()))) {
                throw new InvalidDataException(HttpStatus.BAD_REQUEST, "자주 가는 지역이 아닙니다.");
            }

            weather.setRegionCode(dto.getRegionCode());

            // 현재 시간 구간
            weather.setDayParts(dayParts);

            // 오늘 날짜
            weather.setWeatherDt(currentDate);

            weather.setUserId(userId);

            // DB 저장
            Weather savedWeather = weatherRepository.save(weather);
            response.setResponse(HttpStatus.CREATED.value(), "Success", new WeatherInsertResponse(savedWeather.getWeatherId()));
            httpServletResponse.setStatus(HttpStatus.CREATED.value());

        } catch (InvalidDataException e) {
            log.warn("Exception during weather insert", e);
            throw e;
        }
        catch (Exception e) {
            log.warn("Exception during weather insert", e);
            throw new Exception("동네 날씨 저장에 실패하였습니다");
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

    @Transactional(rollbackFor = {Exception.class})
    public ResponseObject<?> updateWeather(WeatherUpdateRequest dto, Long userId, String token) throws Exception {
        ResponseObject<?> response = new ResponseObject<>();

        try {
            // 동네 날씨 조회
            Weather weather = weatherRepository.findById(dto.getWeatherId()).orElseThrow(
                    () -> new InvalidDataException(HttpStatus.NOT_FOUND, "등록한 날씨 정보가 없습니다."));

            // 본인 확인
            if(!userId.equals(weather.getUserId())) {
                throw new InvalidDataException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
            }

            // 사용자 자주 가는 지역 확인
            List<ExternalLocation> userLocationList = externalApiService.getUserLocationList(token);
            if(userLocationList.stream().noneMatch(item -> dto.getRegionCode().toString().equals(item.getCode()))) {
                throw new InvalidDataException(HttpStatus.BAD_REQUEST, "자주 가는 지역이 아닙니다.");
            }
            weather.setRegionCode(dto.getRegionCode());

            // 날씨 공통코드 확인
            CommonCode commonCode = commonCodeRepository.findByCode(dto.getWeatherCode())
                    .orElseThrow(() ->  new InvalidDataException(HttpStatus.BAD_REQUEST, "날씨 유형이 없습니다."));
            weather.setWeatherCommonCode(commonCode);

            response.setResponse(HttpStatus.OK.value(), "Success");
        } catch (InvalidDataException e) {
            log.warn("Exception during weather update", e);
            throw e;
        }
        catch (Exception e) {
            log.warn("Exception during weather update", e);
            throw new Exception("동네 날씨 수정에 실패하였습니다");
        }

        return response;
    }

    @Transactional(rollbackFor = {Exception.class})
    public ResponseObject<?> deleteWeather(Long userId, Long weatherId) throws Exception {
        ResponseObject<?> response = new ResponseObject<>();

        try{
            // 동네 날씨 조회
            Weather weather = weatherRepository.findById(weatherId).orElseThrow(
                    () -> new InvalidDataException(HttpStatus.NOT_FOUND, "등록한 날씨 정보가 없습니다."));

            // 본인 확인
            if(!userId.equals(weather.getUserId())) {
                throw new InvalidDataException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
            }

            // 삭제
            weatherRepository.delete(weather);
            response.setResponse(HttpStatus.OK.value(), "Success");
        } catch (InvalidDataException e) {
            log.warn("Exception during weather delete", e);
            throw e;
        }
        catch (Exception e) {
            log.warn("Exception during weather delete", e);
            throw new Exception("삭제에 실패하였습니다");
        }

        return response;
    }

    public ResponseObject<WeatherSearchResponse> getMyWeather(Long userId, Long regionCode) throws Exception {
        ResponseObject<WeatherSearchResponse> response = new ResponseObject<>();

        try{
            int dayParts = getDayParts();
            Date currentDate = Date.valueOf(LocalDate.now());

            WeatherSearchResponse data = weatherRepository.searchWeather(userId, regionCode, dayParts, currentDate)
                    .orElseThrow(() -> new InvalidDataException(HttpStatus.NOT_FOUND, "등록한 날씨 정보가 없습니다."));

            response.setResponse(HttpStatus.OK.value(), "Success", data);
        } catch (InvalidDataException e) {
            log.warn("Exception during weather count search", e);
            throw e;
        } catch (Exception e) {
            log.warn("Exception during weather count search", e);
            throw new Exception("동네 날씨 조회에 실패하였습니다");
        }

        return response;
    }

    @Scheduled(cron = "0 0 0,6,12,18 * * *") // 매일 0, 6, 12, 18시
    public void executeServiceLogic() {
        try {
            WeatherRegEvent event = new WeatherRegEvent(getDayParts());
            kafkaProducer.sendWeatherRegEvent(event);
        } catch (Exception e) {
            log.warn("Exception during send weather reg event");
        }

    }
}
