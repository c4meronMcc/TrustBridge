package com.trustbridge.Features.Notifications.Listeners;

import com.trustbridge.Domain.Entities.Jobs;
import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Enums.EmailTemplateType;
import com.trustbridge.Domain.Enums.MilestoneStatus;
import com.trustbridge.Domain.Repositories.JobRepository;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Features.Jobs.Events.UnlockNextMilestoneEvent;
import com.trustbridge.Features.Notifications.Services.EmailSenderService;
import com.trustbridge.Features.Notifications.Services.EmailServiceImpl;
import com.trustbridge.Features.Notifications.Services.TemplateEngineService;
import com.trustbridge.Features.Payments.Events.MilestoneFundedEvent;
import com.trustbridge.Features.Payments.Events.PaymentRequestCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEmailListener {

    private final MilestoneRepository milestoneRepository;
    private final EmailServiceImpl paymentEmailServiceImpl;
    private final TemplateEngineService templateEngineService;
    private final EmailSenderService emailSenderService;


    /**
     * Sends an email to the Freelancer when a Milestone is funded
     *
     * @param event hands over the necessary data for the method to send the email
     * @author Cameron Mccreadie Chaplin
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMilestoneFunded(MilestoneFundedEvent event) {
        log.info("received alert for Milestone Funding {}. Preparing email...", event.getMilestoneId());

        Milestones milestone = milestoneRepository.findById(event.getMilestoneId()).orElseThrow();
        String freelancerEmail = milestone.getJob().getFreelancer().getEmail();

        Map<String, Object> emailData = Map.of(
                "freelancerName", milestone.getJob().getFreelancer().getFirstName() + " " + milestone.getJob().getFreelancer().getLastName(),
                "clientName", milestone.getJob().getClient().getFirstName() + " " + milestone.getJob().getClient().getLastName(),
                "amount", milestone.getAmount(),
                "milestoneTitle", milestone.getTitle()
        );

        String htmlBody = templateEngineService.processTemplate("milestone-paid.html", emailData);

        paymentEmailServiceImpl.sendEmail(
                freelancerEmail,
        "Payment Received",
                EmailTemplateType.MILESTONE_FUNDED_FREELANCER_NOTICE,
                htmlBody,
                milestone.getId()
        );

        log.info("✅ Email successfully sent to Freelancer!");
    }

    @Async
    @TransactionalEventListener
    public void handleMilestoneAwaitingForPayment(PaymentRequestCreatedEvent event) {
        log.info("received alert for milestone awaiting payment. Preparing email...", event.getMilestoneId());

        Milestones milestone = milestoneRepository.findById(event.getMilestoneId()).orElseThrow();

        if (milestone.getSequenceOrder().equals(1)) {
            return;
        }

        String clientEmail = milestone.getJob().getClient().getEmail();

        String frontendJobUrl = "http://localhost:3000/dashboard/client/pay/" + event.getPaymentRequestId();

        Map<String, Object> emailData = Map.of(
                "clientName", milestone.getJob().getClient().getFirstName() + " " + milestone.getJob().getClient().getLastName(),
                "amount", milestone.getAmount(),
                "milestoneTitle", milestone.getTitle(),
                "paymentUrl", frontendJobUrl
        );

        String htmlBody = templateEngineService.processTemplate("milestone-funding-request.html", emailData);


        paymentEmailServiceImpl.sendEmail(
                clientEmail,
                "x",
                EmailTemplateType.MILESTONE_FUNDING_REQUEST,
                htmlBody,
                milestone.getId()
        );

        log.info("Email successfully sent to Client!");
    }


}
