package com.trustbridge.Features.Notifications.Listeners;

import com.trustbridge.Domain.Enums.EmailTemplateType;
import com.trustbridge.Features.Jobs.Dto.JobCreationDto;
import com.trustbridge.Features.Notifications.Services.EmailServiceImpl;
import com.trustbridge.Features.Notifications.Services.TemplateEngineService;

import java.util.Map;
import java.util.UUID;

public class JobEmailListener {

    EmailServiceImpl emailServiceImpl;
    TemplateEngineService templateEngineService;

    public void onJobCreation(JobCreationDto dto, String inviteLink, UUID jobId) {

        Map<String, Object> emailData = Map.of(
                "clientFirstName", dto.clientFirstName() != null ? dto.clientFirstName() : "there",
                "jobTitle", dto.title(),
                "description", dto.description(),
                "currency", dto.currency(),
                "amount", dto.amount(),
                "inviteLink", inviteLink
        );

        String htmlBody = templateEngineService.processTemplate("job-invite.html", emailData);

        emailServiceImpl.sendEmail(
                dto.clientEmail(),
                "Project Proposal: " + dto.title(),
                EmailTemplateType.JOB_INVITATION,
                htmlBody,
                jobId);
        System.out.println("Automated email sent to: " + dto.clientEmail());
    }

}
