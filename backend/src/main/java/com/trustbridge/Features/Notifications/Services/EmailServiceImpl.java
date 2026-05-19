package com.trustbridge.Features.Notifications.Services;


import com.trustbridge.Domain.Entities.EmailLog;
import com.trustbridge.Domain.Enums.EmailStatus;
import com.trustbridge.Domain.Enums.EmailTemplateType;
import com.trustbridge.Domain.Repositories.EmailLogRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("!prod")
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailSenderService {

    private JavaMailSender mailSender;
    private EmailLogRepository auditLogRepository;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public void sendEmail(String toAddress, String subject, EmailTemplateType emailTemplateType, String htmlBody) {

        log.info("Connecting to SMTP server to send email to: {}", toAddress);

        EmailLog auditLog = EmailLog.builder()
                .recipientEmail(toAddress)
                .subject(subject)
                .templateType(emailTemplateType)
                .build();


        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);

            auditLog.setStatus(EmailStatus.DELIVERED);
            auditLogRepository.save(auditLog);

            log.info("Email sent successfully to: {}", toAddress);

        } catch (Exception e) {
            log.error("Critical SMTP error sending to {}: {}", toAddress, e.getMessage());

            auditLog.setStatus(EmailStatus.FAILED);
            auditLog.setErrorMessage(e.getMessage());
            auditLogRepository.save(auditLog);

            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}
