package com.oha.posting.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oha.posting.config.exception.InvalidDataException;
import com.oha.posting.config.response.StatusCode;
import com.oha.posting.dto.external.ExternalLocation;
import com.oha.posting.dto.external.ExternalUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class ExternalApiService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CacheService cacheService;

    @Value("${api.url}")
    private String baseUrl;

    public Map<String, Object> get(String token, String uri) throws IOException {
        return get(token, uri, new HashMap<>());
    }

    public Map<String, Object> get(String token, String uri, Map<String, Object> params) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + uri);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            builder.queryParam(entry.getKey(), entry.getValue());
        }

        ResponseEntity<String> response = restTemplate.exchange(
                builder.build().toUri(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        log.info("External api calL(GET): "+ builder.build().toUri());

        return objectMapper.readValue(response.getBody(), Map.class);
    }

    public Map<String, Object> post(String token, String uri, Map<String, Object> body) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl+uri,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        log.info("External api call(POST): "+ baseUrl+uri);

        return objectMapper.readValue(response.getBody(), Map.class);
    }

    // 같은 격자의 행정구역 조회
    @Cacheable(value = "location-nearby", key="#regionCode")
    public List<ExternalLocation> getLocationList(String token, Long regionCode) {
        try {
            Map<String, Object> responseBody = get(token, "/api/common/location/samegrid/"+regionCode);

            if (!Integer.valueOf(200).equals(responseBody.get("statusCode"))) {
                throw new InvalidDataException(StatusCode.BAD_REQUEST,"행정구역이 존재하지 않습니다.");
            } else {
                return objectMapper.convertValue(responseBody.get("data"), new TypeReference<>() {});
            }
        }
        catch (Exception e) {
            throw new InvalidDataException(StatusCode.BAD_REQUEST,"행정구역이 존재하지 않습니다.");
        }
    }

    // 자주 가는 지역 리스트 조회
    public List<ExternalLocation> getUserLocationList(String token) {
        try {
            Map<String, Object> responseBody = get(token, "/api/common/location/freqdistrict");

            if (!Integer.valueOf(200).equals(responseBody.get("statusCode"))) {
                throw new InvalidDataException(StatusCode.BAD_REQUEST,"사용자 위치가 존재하지 않습니다.");
            } else {
                return objectMapper.convertValue(responseBody.get("data"), new TypeReference<>() {});
            }
        }
        catch (Exception e) {
            throw new InvalidDataException(StatusCode.BAD_REQUEST,"사용자 위치가 존재하지 않습니다.");
        }
    }

    // 행정구역 조회
    @Cacheable(value = "location-single", key="#regionCode")
    public ExternalLocation getLocation(String token, Long regionCode) {
        try {
            Map<String, Object> responseBody = get(token, "/api/common/location/getnamebycode/"+regionCode);
            if (!Integer.valueOf(200).equals(responseBody.get("statusCode"))) {
                throw new InvalidDataException(StatusCode.BAD_REQUEST,"행정구역이 존재하지 않습니다.");
            }
            else {
                return objectMapper.convertValue(responseBody.get("data"), ExternalLocation.class);
            }
        }
        catch (Exception e) {
            throw new InvalidDataException(StatusCode.BAD_REQUEST,"행정구역이 존재하지 않습니다.");
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
                body.put("userIds", userIds);

                Map<String, Object> responseBody = post(token, "/api/user/specificUsers", body);
                if (!Integer.valueOf(201).equals(responseBody.get("statusCode"))) {
                    throw new InvalidDataException(StatusCode.BAD_REQUEST, "사용자가 존재하지 않습니다.");
                } else {
                    List<ExternalUser> userList = objectMapper.convertValue(responseBody.get("data"), new TypeReference<>() {});
                    cacheService.insertUserCache(userList);
                    cachedUserList.addAll(userList);
                }
            } catch (Exception e) {
                throw new InvalidDataException(StatusCode.BAD_REQUEST, "사용자가 존재하지 않습니다.");
            }
        }
        return cachedUserList;
    }
}
