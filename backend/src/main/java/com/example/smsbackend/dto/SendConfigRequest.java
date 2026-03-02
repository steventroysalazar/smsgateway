package com.example.smsbackend.dto;

import jakarta.validation.constraints.NotNull;

public record SendConfigRequest(
    @NotNull Long deviceId,
    String contactNumber,
    String smsPassword,
    Boolean requestLocation,
    Boolean wifiEnabled,
    Integer micVolume,
    Integer speakerVolume,
    Boolean prefixEnabled,
    String prefixName,
    Boolean checkBattery,
    Boolean fallDownEnabled,
    Integer fallDownSensitivity,
    Boolean fallDownCall,
    Boolean noMotionEnabled,
    String noMotionTime,
    Boolean noMotionCall,
    Boolean apnEnabled,
    String apn,
    Boolean serverEnabled,
    String serverHost,
    Integer serverPort,
    Boolean gprsEnabled,
    String workingMode,
    String workingModeInterval,
    String workingModeNoMotionInterval,
    String continuousLocateInterval,
    String continuousLocateDuration,
    Boolean checkStatus
) {
}
