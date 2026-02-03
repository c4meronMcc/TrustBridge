package com.trustbridge.Features.Jobs;

import com.trustbridge.Domain.Entities.Jobs;
import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Enums.JobStatus.*;
import com.trustbridge.Domain.Repositories.JobRepository;
import com.trustbridge.Domain.Repositories.UserRepository;
import com.trustbridge.Features.Auth.RegistrationService;
import com.trustbridge.Features.Jobs.Dto.JobCreationDto;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.apache.commons.lang3.RandomStringUtils;

@Service
public class JobService {

    JobRepository jobRepository;
    UserRepository userRepository;
    RegistrationService registrationService;
    EmailService emailService;

    private final String BASE_URL = "https://localhost:8080/invite/";

    public JobService(JobRepository jobRepository, UserRepository userRepository, RegistrationService registrationService,  EmailService emailService) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.registrationService = registrationService;
        this.emailService = emailService;
    }

    @Transactional
    public String processNewJobOffer(@Valid JobCreationDto dto) {
        String token = generateInviteToken();
        String inviteLink = BASE_URL + token;

        Users client = null;
        if (hasEmail(dto.clientEmail())) {
            // Find existing OR create new guest
            // NOTE: Ensure your RegistrationService.createGuestUser accepts the DTO or specific fields
            client = userRepository.findByEmail(dto.clientEmail())
                    .orElseGet(() -> registrationService.createGuestUser(dto));
        }



        saveNewJob(dto, token, client);

        if (client != null) {
            sendNotificationEmail(dto, inviteLink);
        }

        return inviteLink;
    }

    @Transactional
    public void saveNewJob(@Valid JobCreationDto dto, String token, Users client) {
        Users freelancer = userRepository.findByEmail(dto.freelancerEmail())
                .orElseThrow(() -> new RuntimeException("Freelancer not found"));

        Jobs newJob = Jobs.builder()
                .freelancer(freelancer)
                .client(client)
                .title(dto.title())
                .description(dto.description())
                .totalAmount(dto.amount())
                .currency(dto.currency())
                .countryCode(dto.countryCode())
                .inviteToken(token)
                .status(jobStatus.PENDING_ACCEPTANCE)
                .build();

        jobRepository.save(newJob);
    }

    @Transactional
    public void saveDraftJob(@Valid @RequestBody JobCreationDto dto){
        Users freelancer = userRepository.findByEmail(dto.freelancerEmail()).orElse(null);
        Users client = (dto.clientEmail() != null)
                ? userRepository.findByEmail(dto.clientEmail()).orElse(null)
                : null;

        Jobs DraftJob = Jobs.builder()
                .freelancer(freelancer)
                .client(client)
                .title(dto.title())
                .description(dto.description())
                .totalAmount(dto.amount())
                .currency(dto.currency())
                .countryCode(dto.countryCode())
                .inviteToken(generateInviteToken())
                .status(jobStatus.DRAFT)
                .build();

        jobRepository.save(DraftJob);
    }

    //TODO: create delete job transactional

    @Async
    private void sendNotificationEmail(JobCreationDto dto, String inviteLink) {
        String body = buildEmailTemplate(dto, inviteLink);
        emailService.send(dto.clientEmail(), "Project Proposal: " + dto.title(), body);
        System.out.println("Automated email sent to: " + dto.clientEmail());
    }

    private String buildEmailTemplate(JobCreationDto dto, String inviteLink) {
        String logoUrl = "https://github.com/c4meronMcc/TrustBridge/blob/jobcreation/assets/TrustBridgeLogo.png?raw=true";

        return """
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                    
                    <div style="text-align: center; padding-bottom: 20px;">
                        <img src="%s" alt="Logo" height="25" style="height: 25px; width: auto; vertical-align: middle; display: inline-block; border: 0;">
    
                        <span style="font-family: 'Helvetica Neue', Arial, sans-serif; font-size: 26px; font-weight: 700; color: #333333; vertical-align: middle; display: inline-block; letter-spacing: -0.5px;">
                            TrustBridge
                        </span>
                    </div>
                    
                    <hr style="border: 0; border-top: 1px solid #eeeeee; margin: 0 0 20px 0;">
                    
                    <h3 style="color: #2c3e50; margin-top: 0;">New Project Proposal</h3>
                    <p style="color: #555555; font-size: 16px; line-height: 1.5;">
                        Hi <strong>%s</strong>,
                    </p>
                    <p style="color: #555555; font-size: 16px; line-height: 1.5;">
                        You have received a new proposal for the project <strong>"%s"</strong>.
                    </p>
                    
                    <div style="background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <p style="margin: 5px 0; color: #333;"><strong>Description:</strong> %s</p>
                        <p style="margin: 5px 0; color: #333;"><strong>Total Amount:</strong> %s %.2f</p>
                    </div>

                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #007bff; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold; font-size: 16px; display: inline-block;">
                            Review & Accept Proposal
                        </a>
                    </div>
                    
                    <p style="color: #999999; font-size: 12px; text-align: center; margin-top: 30px;">
                        If the button above doesn't work, copy this link into your browser:<br>
                        <a href="%s" style="color: #007bff; word-break: break-all;">%s</a>
                    </p>
                </div>
            </body>
            </html>
            """.formatted(
                logoUrl,
                dto.clientFirstName() != null ? dto.clientFirstName() : "there",
                dto.title(),
                dto.description(),
                dto.currency(),
                dto.amount(),
                inviteLink,
                inviteLink,
                inviteLink
        );
    }

    public String generateInviteToken() {
        return RandomStringUtils.randomAlphanumeric(64);
    }

    private boolean hasEmail(String  email) {
        return email != null && !email.isBlank();
    }
}
