package com.trustbridge.Features.Notifications.Services;

public interface EmailSenderServiceImpl {
    void sendEmail(String to, String subject, String body);
}
