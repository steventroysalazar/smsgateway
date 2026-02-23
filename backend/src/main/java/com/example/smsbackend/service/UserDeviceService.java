package com.example.smsbackend.service;

import com.example.smsbackend.dto.CreateDeviceRequest;
import com.example.smsbackend.dto.CreateUserRequest;
import com.example.smsbackend.entity.AppUser;
import com.example.smsbackend.entity.Device;
import com.example.smsbackend.repository.AppUserRepository;
import com.example.smsbackend.repository.DeviceRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDeviceService {

    private final AppUserRepository appUserRepository;
    private final DeviceRepository deviceRepository;

    public UserDeviceService(AppUserRepository appUserRepository, DeviceRepository deviceRepository) {
        this.appUserRepository = appUserRepository;
        this.deviceRepository = deviceRepository;
    }

    @Transactional
    public AppUser createUser(CreateUserRequest request) {
        appUserRepository.findByEmailIgnoreCase(request.email().trim()).ifPresent(existing -> {
            throw new IllegalArgumentException("User with that email already exists.");
        });

        AppUser user = new AppUser();
        user.setName(request.name().trim());
        user.setEmail(request.email().trim());
        return appUserRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<AppUser> listUsers() {
        return appUserRepository.findAll();
    }

    @Transactional
    public Device createDevice(Long userId, CreateDeviceRequest request) {
        AppUser user = appUserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Device device = new Device();
        device.setUser(user);
        device.setName(request.name().trim());
        device.setPhoneNumber(request.phoneNumber().trim());
        return deviceRepository.save(device);
    }

    @Transactional(readOnly = true)
    public List<Device> listUserDevices(Long userId) {
        if (!appUserRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found.");
        }
        return deviceRepository.findByUserIdOrderByNameAsc(userId);
    }

    @Transactional(readOnly = true)
    public Device getDevice(Long deviceId) {
        return deviceRepository.findById(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found."));
    }
}
