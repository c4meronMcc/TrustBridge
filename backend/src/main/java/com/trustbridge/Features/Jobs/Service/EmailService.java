package com.trustbridge.Features.Jobs.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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

    public void sendBankTransferDetails(
            String to,
            String milestoneTitle,
            String sortCode,
            String accountNumber,
            String reference,
            BigDecimal amount) {

        String subject = "Payment Instructions for: " + milestoneTitle;

        String body = """
            <html>
            <body style="font-family: Arial, sans-serif; color: #333;">
                <h2>Payment Instructions</h2>
                <p>Please make a bank transfer for the following milestone:</p>
                <p><strong>%s</strong></p>
                <table style="border-collapse: collapse; width: 100%%; max-width: 400px;">
                    <tr style="background-color: #f5f5f5;">
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Amount</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">£%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Sort Code</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                    </tr>
                    <tr style="background-color: #f5f5f5;">
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Account Number</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Reference</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                    </tr>
                </table>
                <p style="color: #e74c3c;"><strong>Important:</strong> You must use the exact reference above
                so your payment is matched correctly.</p>
                <p>Once your transfer is received, funds will be held securely in escrow until
                the milestone is approved.</p>
                <p>Thanks,<br/>The TrustBridge Team</p>
            </body>
            </html>
            """.formatted(milestoneTitle, amount.toPlainString(), sortCode, accountNumber, reference);

        send(to, subject, body);
    }
}
