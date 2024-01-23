package com.oha.posting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class ExternalApiService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${api.url}")
    private String baseUrl;

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

        return objectMapper.readValue(response.getBody(), Map.class);
    }

}
