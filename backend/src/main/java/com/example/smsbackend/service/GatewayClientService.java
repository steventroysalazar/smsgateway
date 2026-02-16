package com.example.smsbackend.service;

import com.example.smsbackend.config.GatewayProperties;
import com.example.smsbackend.dto.GatewayReplyMessage;
import com.example.smsbackend.dto.SendMessageRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GatewayClientService {

    private final GatewayProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GatewayClientService(GatewayProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    public void sendMessage(SendMessageRequest request) {
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SendMessageRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            properties.baseUrl(),
            HttpMethod.POST,
            entity,
            String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Gateway send failed: HTTP " + response.getStatusCode().value());
        }
    }

    public List<GatewayReplyMessage> fetchMessages(String phone, Long since, Integer limit) {
        String url = UriComponentsBuilder.fromHttpUrl(properties.baseUrl())
            .path("/messages")
            .queryParamIfPresent("phone", optional(phone))
            .queryParamIfPresent("since", optional(since))
            .queryParam("limit", limit != null ? limit : properties.defaultLimit())
            .build(true)
            .toUriString();

        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse gateway response", e);
        }
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.hasText(properties.token())) {
            headers.set(HttpHeaders.AUTHORIZATION, properties.token());
        }
        return headers;
    }

    private static <T> java.util.Optional<T> optional(T value) {
        if (value == null) {
            return java.util.Optional.empty();
        }
        if (value instanceof String s && s.isBlank()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(value);
    }
}
