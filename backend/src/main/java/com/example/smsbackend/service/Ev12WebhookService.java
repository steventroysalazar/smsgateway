package com.example.smsbackend.service;

import com.example.smsbackend.config.WebhookProperties;
import com.example.smsbackend.dto.Ev12WebhookEventResponse;
import com.example.smsbackend.entity.Ev12WebhookEvent;
import com.example.smsbackend.repository.Ev12WebhookEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class Ev12WebhookService {

    private final Ev12WebhookEventRepository repository;
    private final WebhookProperties webhookProperties;
    private final ObjectMapper objectMapper;

    public Ev12WebhookService(
        Ev12WebhookEventRepository repository,
        WebhookProperties webhookProperties,
        ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.webhookProperties = webhookProperties;
        this.objectMapper = objectMapper;
    }

    public Ev12WebhookEventResponse ingest(JsonNode payload, String providedToken) {
        validateToken(providedToken);
        String deviceId = readText(payload, "Services Command", "Device ID");
        String imei = readText(payload, "Configuration Command", "IMEI");

        if (!StringUtils.hasText(deviceId) && !StringUtils.hasText(imei)) {
            throw new IllegalArgumentException("Payload must include Services Command.Device ID or Configuration Command.IMEI");
        }

        Ev12WebhookEvent event = new Ev12WebhookEvent();
        event.setDeviceId(StringUtils.hasText(deviceId) ? deviceId : imei);
        event.setImei(imei);
        event.setBatteryLevel(readInteger(payload, "Services Command", "General Data", "battery"));
        event.setSignalQuality(readText(payload, "Services Command", "General Data", "signalStrength", "quality"));
        event.setDeviceTimestamp(readInstant(payload, "Services Command", "General Data", "timestamp"));
        event.setReceivedAt(Instant.now());
        event.setPayloadJson(serialize(payload));

        Ev12WebhookEvent saved = repository.save(event);
        return toResponse(saved);
    }

    public List<Ev12WebhookEventResponse> recentEvents(int limit, String providedToken) {
        validateToken(providedToken);
        int normalizedLimit = Math.max(1, Math.min(limit, 100));
        return repository.findTop100ByOrderByReceivedAtDesc().stream()
            .limit(normalizedLimit)
            .map(this::toResponse)
            .toList();
    }

    private void validateToken(String providedToken) {
        if (!StringUtils.hasText(webhookProperties.ev12Token())) {
            return;
        }
        if (!StringUtils.hasText(providedToken) || !webhookProperties.ev12Token().equals(providedToken.trim())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid webhook token");
        }
    }

    private Ev12WebhookEventResponse toResponse(Ev12WebhookEvent event) {
        return new Ev12WebhookEventResponse(
            event.getId(),
            event.getDeviceId(),
            event.getImei(),
            event.getBatteryLevel(),
            event.getSignalQuality(),
            event.getDeviceTimestamp(),
            event.getReceivedAt(),
            event.getPayloadJson()
        );
    }

    private String serialize(JsonNode payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON payload");
        }
    }

    private String readText(JsonNode payload, String... path) {
        JsonNode node = resolve(payload, path);
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText(null);
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Integer readInteger(JsonNode payload, String... path) {
        JsonNode node = resolve(payload, path);
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isInt() || node.isLong()) {
            return node.intValue();
        }
        String raw = node.asText("").trim();
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Instant readInstant(JsonNode payload, String... path) {
        String timestamp = readText(payload, path);
        if (!StringUtils.hasText(timestamp)) {
            return null;
        }

        try {
            return Instant.parse(timestamp);
        } catch (Exception e) {
            return null;
        }
    }

    private JsonNode resolve(JsonNode payload, String... path) {
        JsonNode current = payload;
        for (String segment : path) {
            if (current == null) {
                return null;
            }
            current = current.path(segment);
        }
        return current;
    }
}
