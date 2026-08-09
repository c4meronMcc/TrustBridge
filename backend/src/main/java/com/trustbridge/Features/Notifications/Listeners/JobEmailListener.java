package com.trustbridge.Features.Notifications.Listeners;

import com.trustbridge.Domain.Enums.EmailTemplateType;
import com.trustbridge.Features.Jobs.Dto.JobCreationDto;
import com.trustbridge.Features.Notifications.Services.EmailServiceImpl;
import com.trustbridge.Features.Notifications.Services.TemplateEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class JobEmailListener {

    EmailServiceImpl emailServiceImpl;
    TemplateEngineService templateEngineService;

    /**
     * Sends an email to the Client when a Job is created
     * @param dto all the data needed about the job
     * @param inviteLink the link to the job
     * @param jobId the id of the job
     * @author Cameron Mccreadie Chaplin
     * **/
    @Async
    @TransactionalEventListener
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
