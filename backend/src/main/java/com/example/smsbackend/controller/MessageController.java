package com.example.smsbackend.controller;

import com.example.smsbackend.dto.GatewayReplyMessage;
import com.example.smsbackend.dto.SendMessageRequest;
import com.example.smsbackend.dto.SendMessageResponse;
import com.example.smsbackend.service.GatewayClientService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final GatewayClientService gatewayClientService;

    public MessageController(GatewayClientService gatewayClientService) {
        this.gatewayClientService = gatewayClientService;
    }

    @PostMapping("/send")
    public ResponseEntity<SendMessageResponse> send(@Valid @RequestBody SendMessageRequest request) {
        gatewayClientService.sendMessage(request);
        return ResponseEntity.ok(new SendMessageResponse(true, "Message sent"));
    }

    @GetMapping("/replies")
    public ResponseEntity<List<GatewayReplyMessage>> replies(
        @RequestParam(required = false) String phone,
        @RequestParam(required = false) Long since,
        @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(gatewayClientService.fetchMessages(phone, since, limit));
    }
}
