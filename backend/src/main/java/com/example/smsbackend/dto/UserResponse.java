package com.example.smsbackend.dto;

public record UserResponse(
    Long id,
    String name,
    String email
) {
}
