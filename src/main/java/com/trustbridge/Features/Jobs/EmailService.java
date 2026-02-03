package com.trustbridge.Features.Jobs;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(String to, String subject, String body) {
        try {
            // 1. Create a MIME message
            MimeMessage message = mailSender.createMimeMessage();

            // 2. Use Helper (true = multipart mode for attachments/html)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("noreply@trustbridge.com");
            helper.setTo(to);
            helper.setSubject(subject);

            // 3. Set the HTML content (true = isHtml)
            helper.setText(body, true);

            mailSender.send(message);
            System.out.println("✅ HTML Email sent successfully to: " + to);

        } catch (MessagingException e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
        }
    }

}
