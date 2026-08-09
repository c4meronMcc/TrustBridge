package com.trustbridge.Features.Notifications.Services;

import com.trustbridge.Domain.Enums.EmailTemplateType;

import java.util.UUID;


public interface EmailSenderService {
    void sendEmail(String to, String subject, EmailTemplateType emailTemplateType, String body, UUID relatedEntityId);
}
