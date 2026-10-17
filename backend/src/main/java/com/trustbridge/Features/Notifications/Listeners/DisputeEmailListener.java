package com.trustbridge.Features.Notifications.Listeners;

import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Enums.EmailTemplateType;
import com.trustbridge.Features.Disputes.Dto.DisputeCreationDto;
import com.trustbridge.Features.Disputes.Dto.DisputeCreationEmailSenderDto;
import com.trustbridge.Features.Notifications.Services.EmailSenderService;
import com.trustbridge.Features.Notifications.Services.TemplateEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class DisputeEmailListener {

    private final EmailSenderService emailSenderService;
    private final TemplateEngineService templateEngineService;
    
    public void clientOpendedDispute(DisputeCreationEmailSenderDto dto, String inviteToken, String disputeId, Users freelancer) {

        String inviteLink = "http://localhost:3000/dispute/" + inviteToken;

        Map<String, Object> model = Map.of(
                "freelancerFirstName", freelancer.getFirstName(),
                    "milestoneTitle", dto.milestoneTitle(),
                "description", dto.description(),
                "currency", dto.currency(),
                "amount", dto.amount(),
                "proposedAmount", dto.proposedAmount(),
                "inviteLink", inviteLink,
                "disputeId", disputeId
                );

        String htmlBody = templateEngineService.processTemplate("dispute-invite.html", model);

        emailSenderService.sendEmail(
                dto.freelancerEmail(),
                "Client Raised Dispute: " + dto.milestoneTitle(),
                EmailTemplateType.DISPUTE_OPENED,
                htmlBody,
                dto.milestoneId()
        );
    }
}
