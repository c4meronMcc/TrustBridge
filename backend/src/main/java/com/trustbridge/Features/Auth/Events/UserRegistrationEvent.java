package com.trustbridge.Features.Auth.Events;

import java.util.UUID;

public record UserRegistrationEvent(
    String email
) { }
