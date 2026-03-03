package com.example.smsbackend.dto;

import java.time.Instant;

public record Ev12WebhookEventResponse(
    Long id,
    String deviceId,
    String imei,
    Integer batteryLevel,
    String signalQuality,
    Instant deviceTimestamp,
    Instant receivedAt,
    String payloadJson
) {
}
