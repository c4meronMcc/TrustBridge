package com.trustbridge.Features.Notifications.Services;

import com.trustbridge.Domain.Enums.EmailTemplateType;


public interface EmailSenderService {
    void sendEmail(String to, String subject, EmailTemplateType emailTemplateType, String body);
}
