package com.example.smsbackend.controller;

import com.example.smsbackend.dto.CreateDeviceRequest;
import com.example.smsbackend.dto.DeviceResponse;
import com.example.smsbackend.dto.UserResponse;
import com.example.smsbackend.entity.Device;
import com.example.smsbackend.service.AuthService;
import com.example.smsbackend.service.UserDeviceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserDeviceService userDeviceService;

    public UserController(UserDeviceService userDeviceService) {
        this.userDeviceService = userDeviceService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers(
        @RequestParam(required = false) Long managerId
    ) {
        List<UserResponse> response = (managerId == null
            ? userDeviceService.listUsers()
            : userDeviceService.listUsersByManager(managerId))
            .stream()
            .map(AuthService::toUserResponse)
            .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{userId}/devices")
    public ResponseEntity<DeviceResponse> createDevice(
        @PathVariable Long userId,
        @Valid @RequestBody CreateDeviceRequest request
    ) {
        Device device = userDeviceService.createDevice(userId, request);
        return ResponseEntity.ok(new DeviceResponse(
            device.getId(),
            device.getUser().getId(),
            device.getName(),
            device.getPhoneNumber()
        ));
    }

    @GetMapping("/users/{userId}/devices")
    public ResponseEntity<List<DeviceResponse>> listUserDevices(@PathVariable Long userId) {
        List<DeviceResponse> response = userDeviceService.listUserDevices(userId).stream()
            .map(device -> new DeviceResponse(device.getId(), device.getUser().getId(), device.getName(), device.getPhoneNumber()))
            .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/locations/{locationId}/devices")
    public ResponseEntity<List<DeviceResponse>> listLocationDevices(@PathVariable Long locationId) {
        List<DeviceResponse> response = userDeviceService.listDevicesByLocation(locationId).stream()
            .map(device -> new DeviceResponse(device.getId(), device.getUser().getId(), device.getName(), device.getPhoneNumber()))
            .toList();
        return ResponseEntity.ok(response);
    }
}
