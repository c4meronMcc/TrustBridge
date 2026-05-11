package com.trustbridge.Features.Notifications.Services;


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
@Profile("!test")
@RequiredArgsConstructor
public class EmailService implements EmailSenderServiceImpl {

    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public void sendEmail(String toAddress, String subject, String htmlBody) {

        log.info("Connecting to SMTP server to send email to: {}", toAddress);

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);

            log.info("Email sent successfully to: {}", toAddress);

        } catch (Exception e) {
            log.error("Critical SMTP error sending to {}: {}", toAddress, e.getMessage());

            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}
