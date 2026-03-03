package com.example.smsbackend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.smsbackend.config.WebhookProperties;
import com.example.smsbackend.dto.Ev12WebhookEventResponse;
import com.example.smsbackend.entity.Ev12WebhookEvent;
import com.example.smsbackend.repository.Ev12WebhookEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

class Ev12WebhookServiceTest {

    @Mock
    private Ev12WebhookEventRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void ingestShouldExtractDeviceDataAndPersist() throws Exception {
        String body = """
            {
              \"Configuration Command\": { \"IMEI\": \"862667084205114\" },
              \"Services Command\": {
                \"Device ID\": \"862667084205114\",
                \"General Data\": {
                  \"battery\": 96,
                  \"signalStrength\": { \"quality\": \"Good\" },
                  \"timestamp\": \"2026-03-02T05:51:32.000Z\"
                }
              }
            }
            """;

        JsonNode payload = objectMapper.readTree(body);
        when(repository.save(any(Ev12WebhookEvent.class))).thenAnswer(invocation -> {
            Ev12WebhookEvent event = invocation.getArgument(0);
            event.setReceivedAt(Instant.now());
            return event;
        });

        Ev12WebhookService service = new Ev12WebhookService(repository, new WebhookProperties(null), objectMapper);
        Ev12WebhookEventResponse response = service.ingest(payload, null);

        assertEquals("862667084205114", response.deviceId());
        assertEquals("862667084205114", response.imei());
        assertEquals(96, response.batteryLevel());
        assertEquals("Good", response.signalQuality());
        assertNotNull(response.deviceTimestamp());
        assertNotNull(response.payloadJson());
    }

    @Test
    void ingestShouldRequireValidTokenWhenConfigured() throws Exception {
        JsonNode payload = objectMapper.readTree("{\"Services Command\": {\"Device ID\": \"abc\"}}");
        Ev12WebhookService service = new Ev12WebhookService(repository, new WebhookProperties("secret"), objectMapper);

        assertThrows(ResponseStatusException.class, () -> service.ingest(payload, "wrong"));
    }
}
