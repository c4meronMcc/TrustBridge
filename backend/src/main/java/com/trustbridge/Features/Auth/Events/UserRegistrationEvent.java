package com.trustbridge.Features.Auth.Events;

import java.time.OffsetDateTime;

public record UserRegistrationEvent(
    String email,
    String firstName,
    String verificationCode
) { }
