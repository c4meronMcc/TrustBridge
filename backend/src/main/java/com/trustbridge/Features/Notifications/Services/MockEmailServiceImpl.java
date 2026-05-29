package com.trustbridge.Features.Notifications.Services;

import com.trustbridge.Domain.Entities.EmailLog;
import com.trustbridge.Domain.Enums.EmailStatus; // Recommended to import this
import com.trustbridge.Domain.Enums.EmailTemplateType;
import com.trustbridge.Domain.Repositories.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID; // 🚨 Don't forget to import UUID

@Slf4j
@Service
@Profile("dev")
@RequiredArgsConstructor
public class MockEmailServiceImpl implements EmailSenderService {

    private final EmailLogRepository auditLogRepository;

    @Override
    // 🚨 1. Add the UUID relatedEntityId parameter here
    public void sendEmail(String toAddress, String subject, EmailTemplateType emailTemplateType, String body, UUID relatedEntityId) {
        log.info("Mock email (suppressed) to: {} | Subject: {}", toAddress, subject);

        EmailLog auditlog = EmailLog.builder()
                .recipientEmail(toAddress)
                .subject(subject)
                .templateType(emailTemplateType)
                .relatedEntityId(relatedEntityId) // 🚨 2. Save the UUID to the database
                .status(EmailStatus.DELIVERED)    // Optional: good practice to mock the success status too
                .build();

        auditLogRepository.save(auditlog);
    }
}