package com.trustbridge.Features.Auth.Dto;

public record ApiErrorResponse(
        String error,
        String message
) {
}
