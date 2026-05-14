package com.trustbridge.Features.Auth.Dto;

public record RegistrationVerificationDTO(
        String verificationCode,
        String email
) {}
