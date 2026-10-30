package com.trustbridge.Features.Disputes.Dto;

import com.trustbridge.Domain.Entities.Users;

import java.util.UUID;

public record DisputeOpenedResult(
        UUID disputeId,
        String inviteToken,
        DisputeCreationEmailSenderDto emailDto,
        Users freelancer
) { }
