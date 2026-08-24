package com.trustbridge.Features.Notifications.Listeners;

import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Enums.EmailTemplateType;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Features.Notifications.Services.EmailServiceImpl;
import com.trustbridge.Features.Notifications.Services.TemplateEngineService;
import com.trustbridge.Features.Payments.Events.MilestoneSubmittedForApprovalEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MilestoneEmailListener {

    private final MilestoneRepository milestoneRepository;
    private final EmailServiceImpl emailServiceImpl;
    private final TemplateEngineService templateEngineService;

    /**
     * Sends an email to the Freelancer when a Milestone is submitted for approval
     *
     * @param event hands over the necessary data for the method to send the email
     * @author Cameron Mccreadie Chaplin
     * **/
    @Async
    @TransactionalEventListener
    public void handleFreelancerSubmitsMilestone(MilestoneSubmittedForApprovalEvent event) {
        log.info("received alert for Milestone Submission {}. Preparing email...", event.getMilestoneId());

        Milestones milestone = milestoneRepository.findById(event.getMilestoneId()).orElseThrow();

        String clientEmail = milestone.getJob().getClient().getEmail();
        log.info("sending email to {}", clientEmail);


        String frontendReviewUrl = "http://localhost:3000/submission/approval/"
                + milestone.getId()
                + "?jobId=" + milestone.getJob().getId();

        Map<String, Object> emailData = Map.of(
                "freelancerName", milestone.getJob().getFreelancer().getFirstName() + " " + milestone.getJob().getFreelancer().getLastName(),
                "clientName", milestone.getJob().getClient().getFirstName() + " " + milestone.getJob().getClient().getLastName(),
                "amount", milestone.getAmount(),
                "milestoneTitle", milestone.getTitle(),
                "reviewUrl", frontendReviewUrl
        );

        String htmlBody = templateEngineService.processTemplate("freelancer-submitted-milstone-email.html", emailData);

        emailServiceImpl.sendEmail(
                clientEmail,
                "Milestone Awaiting Your Approval",
                EmailTemplateType.WORK_SUBMITTED_FOR_REVIEW,
                htmlBody,
                milestone.getId()
        );

        log.info("Email successfully sent to Client!");
    }

    @Async
    @TransactionalEventListener
    public void handleClientReceivesWaitingForApprovalEmail(MilestoneSubmittedForApprovalEvent event) {
        log.info("received alert for Milestone Submission {}. Preparing email...", event.getMilestoneId());

        Milestones milestone = milestoneRepository.findById(event.getMilestoneId()).orElseThrow();
        String freelancerEmail = milestone.getJob().getFreelancer().getEmail();

        log.info("sending email to {}", freelancerEmail);

        String frontendJobUrl = "http://localhost:3000/dashboard/freelancer/jobs/" + milestone.getJob().getId();

        Map<String, Object> emailData = Map.of(
                "clientName", milestone.getJob().getClient().getFirstName() + " " + milestone.getJob().getClient().getLastName(),
                "freelancerName", milestone.getJob().getFreelancer().getFirstName() + " " + milestone.getJob().getFreelancer().getLastName(),
                "milestoneTitle", milestone.getTitle(),
                "jobUrl", frontendJobUrl
        );

        String htmlBody = templateEngineService.processTemplate("freelancer-notification-that-client-has-received-email.html", emailData);

        emailServiceImpl.sendEmail(
                freelancerEmail,
                "Milestone Awaiting Your Approval",
                EmailTemplateType.WORK_SUBMITTED_FOR_REVIEW,
                htmlBody,
                milestone.getId()
        );

        log.info("Email successfully sent to Freelancer!");
    }

    public void handleClientRequestedChangesToMilestone(UUID milestoneId, String feedback) {
        log.info("received alert for Milestone Submission {}. Preparing email...", milestoneId);
    }
}
