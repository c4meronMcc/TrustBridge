package com.trustbridge.Features.Notifications.Listeners;

import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Enums.EmailTemplateType;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Features.Notifications.Services.EmailServiceImpl;
import com.trustbridge.Features.Notifications.Services.TemplateEngineService;
import com.trustbridge.Features.Payments.Events.MilestoneFundedEvent;
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
    private final EmailServiceImpl paymentEmailServiceImplImpl;
    private final TemplateEngineService templateEngineService;

    /**
     * @Async tells Spring to run this on a separate background thread.
     * @TransactionalEventListener ensures we ONLY send the email AFTER the database
     *                             has successfully saved the Milestone as PAID.
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

        paymentEmailServiceImplImpl.sendEmail(
                freelancerEmail,
        "Payment Received",
                EmailTemplateType.MILESTONE_FUNDED_FREELANCER_NOTICE,
                htmlBody
        );

        log.info("✅ Email successfully sent to Freelancer!");
    }


}
