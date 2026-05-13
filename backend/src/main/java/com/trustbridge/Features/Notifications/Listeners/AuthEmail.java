package com.trustbridge.Features.Notifications.Listeners;

import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Enums.EmailTemplateType;
import com.trustbridge.Domain.Repositories.UserRepository;
import com.trustbridge.Features.Auth.Events.UserRegistrationEvent;
import com.trustbridge.Features.Notifications.Services.EmailService;
import com.trustbridge.Features.Notifications.Services.TemplateEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEmail {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TemplateEngineService templateEngineService;

    @Async
    @TransactionalEventListener
    public void onUserRegistration(UserRegistrationEvent event) {
        log.info("received event for User Registration: {}", event.email());

        Users user = userRepository.findByEmail(event.email()).orElseThrow();
        String userEmail = user.getEmail();

        Map<String, Object> emailData = Map.of(
                "firstName", user.getFirstName(),
                "accountType",  user.getUserRole().name(),
                "verification", user.getVerificationCode()
        );

        String htmlBody = templateEngineService.processTemplate("example.html", emailData);

        emailService.sendEmail(
                userEmail,
                "Verify Account",
                EmailTemplateType.WELCOME_FREELANCER,
                htmlBody
        );

        log.info("Email successfully sent to " + user.getUserRole().name());
    }

}
