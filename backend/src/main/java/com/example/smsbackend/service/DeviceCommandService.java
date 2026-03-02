package com.example.smsbackend.service;

import com.example.smsbackend.dto.SendConfigRequest;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DeviceCommandService {

    public List<String> buildCommands(SendConfigRequest request) {
        List<String> commands = new ArrayList<>();

        if (StringUtils.hasText(request.contactNumber())) {
            commands.add("A1,1,1," + request.contactNumber().trim());
        }
        if (StringUtils.hasText(request.smsPassword())) {
            commands.add("P" + request.smsPassword().trim());
        }
        if (Boolean.TRUE.equals(request.requestLocation())) {
            commands.add("loc");
        }
        if (request.wifiEnabled() != null) {
            commands.add("Wifi" + (request.wifiEnabled() ? 1 : 0));
        }
        if (request.micVolume() != null) {
            commands.add("Micvolume" + request.micVolume());
        }
        if (request.speakerVolume() != null) {
            commands.add("Speakervolume" + request.speakerVolume());
        }
        if (request.prefixEnabled() != null && StringUtils.hasText(request.prefixName())) {
            commands.add("prefix" + (request.prefixEnabled() ? 1 : 0) + "," + request.prefixName().trim());
        }
        if (Boolean.TRUE.equals(request.checkBattery())) {
            commands.add("battery");
        }
        if (request.fallDownEnabled() != null && request.fallDownSensitivity() != null && request.fallDownCall() != null) {
            commands.add("fl" + (request.fallDownEnabled() ? 1 : 0) + "," + request.fallDownSensitivity()
                + "," + (request.fallDownCall() ? 1 : 0));
        }
        if (request.noMotionEnabled() != null && StringUtils.hasText(request.noMotionTime()) && request.noMotionCall() != null) {
            commands.add("nmo" + (request.noMotionEnabled() ? 1 : 0) + "," + request.noMotionTime().trim()
                + "," + (request.noMotionCall() ? 1 : 0));
        }
        if (request.apnEnabled() != null && StringUtils.hasText(request.apn())) {
            commands.add("S" + (request.apnEnabled() ? 1 : 0) + "," + request.apn().trim());
        }
        if (request.serverEnabled() != null && StringUtils.hasText(request.serverHost()) && request.serverPort() != null) {
            commands.add("IP" + (request.serverEnabled() ? 1 : 0) + "," + request.serverHost().trim() + "," + request.serverPort());
        }
        if (request.gprsEnabled() != null) {
            commands.add("S" + (request.gprsEnabled() ? 2 : 0));
        }

        appendWorkingModeCommands(request, commands);

        if (StringUtils.hasText(request.continuousLocateInterval()) && StringUtils.hasText(request.continuousLocateDuration())) {
            commands.add("CL" + request.continuousLocateInterval().trim() + "," + request.continuousLocateDuration().trim());
        }
        if (Boolean.TRUE.equals(request.checkStatus())) {
            commands.add("status");
        }

        return commands;
    }

    public String buildPreview(List<String> commands) {
        return String.join(";", commands);
    }

    public List<String> splitForSms(String preview) {
        if (!StringUtils.hasText(preview)) {
            return List.of();
        }

        int chunkSize = 150;
        List<String> chunks = new ArrayList<>();
        for (int start = 0; start < preview.length(); start += chunkSize) {
            chunks.add(preview.substring(start, Math.min(preview.length(), start + chunkSize)));
        }
        return chunks;
    }

    private void appendWorkingModeCommands(SendConfigRequest request, List<String> commands) {
        if (!StringUtils.hasText(request.workingMode())) {
            return;
        }

        String mode = request.workingMode().trim();
        if ("mode1".equals(mode)) {
            commands.add("mode1");
            return;
        }

        if (("mode2".equals(mode) || "mode6".equals(mode))
            && StringUtils.hasText(request.workingModeInterval())
            && StringUtils.hasText(request.workingModeNoMotionInterval())) {
            commands.add(mode + "," + request.workingModeInterval().trim() + "," + request.workingModeNoMotionInterval().trim());
            return;
        }

        if (("mode3".equals(mode) || "mode4".equals(mode) || "mode5".equals(mode))
            && StringUtils.hasText(request.workingModeInterval())) {
            commands.add(mode + "," + request.workingModeInterval().trim());
        }
    }
}
