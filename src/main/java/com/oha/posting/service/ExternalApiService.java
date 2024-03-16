package com.oha.posting.service;

import com.oha.posting.config.exception.InvalidDataException;
import com.oha.posting.config.response.ResponseObject;
import com.oha.posting.dto.external.ExternalLocation;
import com.oha.posting.dto.external.ExternalUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExternalApiService {
    private final CacheService cacheService;
    private final WebClient webClient;

    public ExternalApiService(CacheService cacheService, WebClient.Builder webClientBuilder, @Value("${api.url}") String baseUrl) {
        this.cacheService = cacheService;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public <T> T get(String uri, String token, ParameterizedTypeReference<T> typeReference) {
        log.info("External api call (GET): " + uri);
        return webClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(typeReference)
                .block();
    }
    public <T> T post(String uri, String token, Map<String, Object> body, ParameterizedTypeReference<T> typeReference) {
        log.info("External api call (POST): " + uri);
        return webClient.post()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, token)
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(typeReference)
                .block();
    }

    // 같은 격자의 행정구역 조회
    @Cacheable(value = "location-nearby", key="#regionCode")
    public List<ExternalLocation> getLocationList(String token, Long regionCode) {
        try {
            ParameterizedTypeReference<ResponseObject<List<ExternalLocation>>> typeReference = new ParameterizedTypeReference<>(){};
            return get("/api/common/location/samegrid/"+regionCode, token, typeReference).getData();

        } catch (Exception e) {
            throw new InvalidDataException(HttpStatus.BAD_REQUEST,"위치 정보를 찾을 수 없습니다.");
        }
    }

    public Map<String, ExternalLocation> getLocationMap(String token, Long regionCode) {
        List<ExternalLocation> locationList = getLocationList(token, regionCode);
        return locationList.stream().collect(Collectors.toMap(ExternalLocation::getCode, location -> location));
    }

    // 자주 가는 지역 리스트 조회
    public List<ExternalLocation> getUserLocationList(String token) {
        try {
            ParameterizedTypeReference<ResponseObject<List<ExternalLocation>>> typeReference = new ParameterizedTypeReference<>(){};
            return get("/api/common/location/freqdistrict", token, typeReference).getData();

        } catch (Exception e) {
            throw new InvalidDataException(HttpStatus.BAD_REQUEST,"자주가는 위치 정보를 찾을 수 없습니다.");
        }
    }

    // 행정구역 조회
    @Cacheable(value = "location-single", key="#regionCode")
    public ExternalLocation getLocation(String token, Long regionCode) {
        try {
            ParameterizedTypeReference<ResponseObject<ExternalLocation>> typeReference = new ParameterizedTypeReference<>(){};
            return get("/api/common/location/getnamebycode/"+regionCode, token, typeReference).getData();

        } catch (Exception e) {
            throw new InvalidDataException(HttpStatus.BAD_REQUEST,"위치 정보를 찾을 수 없습니다.");
        }
    }

    // user 리스트 조회
    public List<ExternalUser> getUserList(String token, Set<Long> userIds) {
        Set<Long> notCachedUserIds = new HashSet<>();
        List<ExternalUser> cachedUserList = new ArrayList<>();
        cacheService.getUserCacheInfo(userIds, notCachedUserIds, cachedUserList);

        if (!notCachedUserIds.isEmpty()) {
            try {
                Map<String, Object> body = new HashMap<>();
                body.put("userIds", notCachedUserIds);

                ParameterizedTypeReference<ResponseObject<List<ExternalUser>>> typeReference = new ParameterizedTypeReference<>(){};
                List<ExternalUser> userList = post("/api/user/specificUsers", token, body, typeReference).getData();

                cacheService.insertUserCache(userList);
                cachedUserList.addAll(userList);

            } catch (Exception e) {
                throw new InvalidDataException(HttpStatus.BAD_REQUEST, "사용자 정보를 찾을 수 없습니다.");
            }
        }
        return cachedUserList;
    }


    public Map<Long, ExternalUser> getUserMap(String token, Set<Long> userIds) {
        List<ExternalUser> userList = getUserList(token, userIds);
        return userList.stream().collect(Collectors.toMap(ExternalUser::getUserId, user -> user));
    }

    public List<ExternalLocation> getLocationList(String token, Set<Long> codes) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("codes", codes);

            ParameterizedTypeReference<ResponseObject<List<ExternalLocation>>> typeReference = new ParameterizedTypeReference<>(){};
            return post("/api/common/location/getnamebycodes", token, body, typeReference).getData();

        } catch (Exception e) {
            throw new InvalidDataException(HttpStatus.BAD_REQUEST,"위치 정보를 찾을 수 없습니다.");
        }
    }

    public Map<String, ExternalLocation> getLocationMap(String token, Set<Long> codes) {
        List<ExternalLocation> locationList = getLocationList(token, codes);
        return locationList.stream().collect(Collectors.toMap(ExternalLocation::getCode, location -> location));
    }
}
