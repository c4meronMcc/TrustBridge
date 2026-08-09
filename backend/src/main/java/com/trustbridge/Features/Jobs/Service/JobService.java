package com.trustbridge.Features.Jobs.Service;

import com.trustbridge.Domain.Entities.Jobs;
import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Enums.EmailTemplateType;
import com.trustbridge.Domain.Enums.JobStatus.*;
import com.trustbridge.Domain.Enums.MilestoneStatus;
import com.trustbridge.Domain.Repositories.JobRepository;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Domain.Repositories.UserRepository;
import com.trustbridge.Features.Auth.Service.RegistrationService;
import com.trustbridge.Features.Jobs.Dto.JobCreationDto;
import com.trustbridge.Features.Jobs.Dto.PaymentActivationDto;
import com.trustbridge.Features.Notifications.Services.EmailServiceImpl;
import com.trustbridge.Features.Payments.Service.PaymentRequestService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final RegistrationService registrationService;
    private final EmailServiceImpl emailServiceImpl;
    private final MilestoneService milestoneService;
    private final JobStateService jobStateService;
    private final MilestoneRepository milestoneRepository;
    private final PaymentRequestService paymentRequestService;


    /**
     * Processes the creation of a new job offer by generating an invite token,
     * creating or retrieving the client user, saving the job information,
     * and sending a notification email if applicable.
     *
     * @param dto the data transfer object containing details of the job offer
     * @param authenticatedEmail the email address of the authenticated user initiating the process
     */
    @Transactional
    public void processNewJobOffer(@Valid JobCreationDto dto, String authenticatedEmail) {
        String token = generateInviteToken();
        String BASE_URL = "http://localhost:3000/invite/"; // Pointing to Next.js frontend
        String inviteLink = BASE_URL + token;

        Users client = null;
        if (hasEmail(dto.clientEmail())) {
            client = userRepository.findByEmail(dto.clientEmail())
                    .orElseGet(() -> registrationService.createGuestUser(dto));
        }

        // Pass the email down to the save method
        Jobs savedJob = saveNewJob(dto, token, client, authenticatedEmail);

        if (client != null) {
            sendNotificationEmail(dto, inviteLink, savedJob.getId());
        }
    }

    /**
     * Saves a new job into the system based on the provided job creation details.
     *
     * @param dto the job creation details containing the job title, description, amount, currency,
     *            and optional milestones
     * @param token a unique token used for job invitation
     * @param client the client user associated with the job
     * @param authenticatedEmail the email of the authenticated freelancer user who will be assigned to the job
     * @return the created job instance
     * @throws RuntimeException if the freelancer is not found, or if the milestone total does not equal the job amount
     */
    @Transactional
    public Jobs  saveNewJob(@Valid JobCreationDto dto, String token, Users client, String authenticatedEmail) {
        Users freelancer = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new RuntimeException("Freelancer not found"));

        Jobs newJob = Jobs.builder()
                .freelancer(freelancer)
                .client(client)
                .title(dto.title())
                .description(dto.description())
                .totalAmount(dto.amount())
                .currency(dto.currency())
                .inviteToken(token)
                .status(jobStatus.PENDING_ACCEPTANCE)
                .build();

        jobRepository.save(newJob);

        if (dto.milestones() != null && !dto.milestones().isEmpty()) {
            if (milestoneTotalValid(dto.milestones(), dto)) {
                throw new RuntimeException("Milestone total does not equal job amount!");
            }
            milestoneService.createMilestones(newJob ,dto.milestones());
        } else {
            JobCreationDto.MilestoneCreationDto defaultMilestone = new JobCreationDto.MilestoneCreationDto(
                    "Project Completion",
                    dto.amount(),
                    1
            );

            milestoneService.createMilestones(newJob, List.of(defaultMilestone));
        }

        return newJob;
    }

    /**
     * Saves a draft job using the provided job creation details and authenticated email.
     *
     * @param dto The job creation details, including title, description, client email, amount, currency, and milestones.
     *            Must be a valid {@link JobCreationDto}.
     * @param authenticatedEmail The email of the authenticated user creating the draft job.
     *                           This email is used to identify the freelancer associated with the draft job.
     * @throws RuntimeException If the total amount of the milestones does not equal the job amount.
     */
    @Transactional
    public void saveDraftJob(@Valid @RequestBody JobCreationDto dto, String authenticatedEmail){
        Users freelancer = userRepository.findByEmail(authenticatedEmail).orElse(null);
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
                .inviteToken(generateInviteToken())
                .status(jobStatus.DRAFT)
                .build();

        jobRepository.save(DraftJob);

        if (dto.milestones() != null && !dto.milestones().isEmpty()) {
            if (milestoneTotalValid(dto.milestones(), dto)) {
                throw new RuntimeException("Milestone total does not equal job amount!");
            }
            milestoneService.createMilestones(DraftJob ,dto.milestones());
        }
    }

    /**
     * Deletes a draft job identified by its unique job ID.
     * This method will only delete a job if its status is set to DRAFT.
     * If the job is not found or its status is not DRAFT, an exception is thrown.
     *
     * @param jobId the unique identifier of the job to be deleted, which must be in DRAFT status
     * @throws RuntimeException if the job is not found or its status is not DRAFT
     */
    @Transactional
    public void deleteDraftJob(@Valid @RequestBody UUID jobId) {

        Jobs job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found!"));

        if (!job.getStatus().equals(jobStatus.DRAFT)) {
            throw new RuntimeException("Job status is not DRAFT!");
        }

        jobRepository.delete(job);
    }

    /**
     * Activates a job identified by the provided invite token. The method transitions
     * the job to an active state if it's in a pending state or validates that it is
     * awaiting payment. It also retrieves the client secret for the first milestone
     * associated with the job to facilitate payment.
     *
     * @param inviteToken the unique token that identifies the job to be activated
     * @return a {@code PaymentActivationDto} containing the client secret for the
     *         first milestone of the job
     * @throws RuntimeException if the invite token is invalid, no milestones are found for the job,
     *                          or a Stripe token could not be generated
     * @throws IllegalStateException if the job*/
    @Transactional
    public PaymentActivationDto activateJob(String inviteToken) {
        Jobs job = jobRepository.findByInviteToken(inviteToken)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (job.getStatus() == jobStatus.PENDING_ACCEPTANCE) {
            jobStateService.pendingToActive(job.getId(), inviteToken);
        } else if (job.getStatus() != jobStatus.AWAITING_PAYMENT) {
            throw new IllegalStateException("Job cannot be accepted in its current state.");
        }

        Milestones firstMilestone = milestoneRepository
                .findFirstByJobIdOrderBySequenceOrderAsc(job.getId()) // 👈 Update this query
                .orElseThrow(() -> new RuntimeException("No milestones found for this job."));

        String clientSecret = paymentRequestService.getClientSecretForMilestone(firstMilestone.getId());

        if (clientSecret == null) {
            throw new RuntimeException("Stripe token was not generated.");
        }

        return new PaymentActivationDto(clientSecret);
    }

    /**
     * Updates the status of a job to PAID_OUT for the given job ID.
     *
     * @param jobId the unique identifier of the job to be updated; must be a valid UUID
     */
    @Transactional
    public void jobStatusToComplete(@Valid @RequestBody UUID jobId) {
        Jobs job = checkJobExists(jobId);

        job.setStatus(jobStatus.PAID_OUT);
        jobRepository.save(job);
    }

    /**
     * Updates the status of a job to CANCELLED for the given job ID.
     *
     * @param jobId the unique identifier of the job whose status is to be updated
     */
    @Transactional
    public void jobStatusToCancelled(@Valid @RequestBody UUID jobId) {
        Jobs job = checkJobExists(jobId);

        job.setStatus(jobStatus.CANCELLED);
        jobRepository.save(job);
    }

    /**
     * Updates the status of a job to PENDING_ACCEPTANCE for the given job ID.
     *
     * @param jobId the unique identifier of the job to be updated
     */
    @Transactional
    public void jobStatusToPendingAccepted(@Valid @RequestBody UUID jobId) {
        Jobs job = checkJobExists(jobId);

        job.setStatus(jobStatus.PENDING_ACCEPTANCE);
        jobRepository.save(job);
    }

    /**
     * Updates the status of a*/
    @Transactional
    public void jobStatusToActive(@Valid @RequestBody UUID jobId) {
        Jobs job = checkJobExists(jobId);

        job.setStatus(jobStatus.IN_PROGRESS);

    }

    /**
     * Updates the status of a job to 'DISPUTED' for the given job ID.
     * This method is transactional and ensures atomicity of the operation.
     *
     * @param jobId the unique identifier of the job to be updated
     *              provided as a valid UUID
     * @throws RuntimeException if the specified job is not found
     */
    @Transactional
    public void jobStatusToDisputed(@Valid @RequestBody UUID jobId) {
        Jobs job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found!"));

        job.setStatus(jobStatus.DISPUTED);
        jobRepository.save(job);
    }

    /**
     * Sends a notification email to the client with the details of a job invitation.
     *
     * @param dto        the data transfer object containing job creation details such as title and client email
     * @param inviteLink the link to invite the client to the job
     * @param jobId      the unique identifier of the job
     */
    @Async
    protected void sendNotificationEmail(JobCreationDto dto, String inviteLink, UUID jobId) {
        String body = buildEmailTemplate(dto, inviteLink);
        emailServiceImpl.sendEmail(dto.clientEmail(), "Project Proposal: " + dto.title(), EmailTemplateType.JOB_INVITATION, body, jobId);
        System.out.println("Automated email sent to: " + dto.clientEmail());
    }

    /**
     * Builds and returns an HTML email template string for a new project proposal.
     *
     * @param dto Data transfer object containing details about the job creation,
     *            including client name, project title, description, currency, and amount.
     * @param inviteLink The link to review and accept the proposal, included in the email body.
     * @return A formatted HTML string representing the email template.
     */
    private String buildEmailTemplate(JobCreationDto dto, String inviteLink) {
        String logoUrl = "https://github.com/c4meronMcc/TrustBridge/blob/jobcreation/assets/TrustBridgeLogo.png?raw=true";

        return """
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                   \s
                    <div style="text-align: center; padding-bottom: 20px;">
                        <img src="%s" alt="Logo" height="25" style="height: 25px; width: auto; vertical-align: middle; display: inline-block; border: 0;">
   \s
                        <span style="font-family: 'Helvetica Neue', Arial, sans-serif; font-size: 26px; font-weight: 700; color: #333333; vertical-align: middle; display: inline-block; letter-spacing: -0.5px;">
                            TrustBridge
                        </span>
                    </div>
                   \s
                    <hr style="border: 0; border-top: 1px solid #eeeeee; margin: 0 0 20px 0;">
                   \s
                    <h3 style="color: #2c3e50; margin-top: 0;">New Project Proposal</h3>
                    <p style="color: #555555; font-size: 16px; line-height: 1.5;">
                        Hi <strong>%s</strong>,
                    </p>
                    <p style="color: #555555; font-size: 16px; line-height: 1.5;">
                        You have received a new proposal for the project <strong>"%s"</strong>.
                    </p>
                   \s
                    <div style="background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <p style="margin: 5px 0; color: #333;"><strong>Description:</strong> %s</p>
                        <p style="margin: 5px 0; color: #333;"><strong>Total Amount:</strong> %s %.2f</p>
                    </div>

                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #007bff; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold; font-size: 16px; display: inline-block;">
                            Review & Accept Proposal
                        </a>
                    </div>
                   \s
                    <p style="color: #999999; font-size: 12px; text-align: center; margin-top: 30px;">
                        If the button above doesn't work, copy this link into your browser:<br>
                        <a href="%s" style="color: #007bff; word-break: break-all;">%s</a>
                    </p>
                </div>
            </body>
            </html>
           \s""".formatted(
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

    /**
     * Generates a random alphanumeric token to be used as an invite token.
     *
     * @return A 64-character long random alphanumeric string.
     */
    public String generateInviteToken() {
        return RandomStringUtils.randomAlphanumeric(64);
    }

    /**
     * Checks if the provided email string is non-null and not blank.
     *
     * @param email the email string to check
     * @return {@code true} if the email is non-null and not blank, {@code false} otherwise
     */
    private boolean hasEmail(String  email) {
        return email != null && !email.isBlank();
    }

    /**
     * Validates whether the total amount of all milestones matches the job amount.
     *
     * @param milestones the list of milestone creation DTOs containing individual milestone amounts
     * @param job the job creation DTO containing the total job amount
     * @return true if the total amount of milestones does not equal the job amount, false otherwise
     */
    private boolean milestoneTotalValid(List<JobCreationDto.MilestoneCreationDto> milestones, JobCreationDto job) {

        List<BigDecimal> milestonesAmounts = milestones.stream()
                .map( milestone -> milestone.amount()).toList();

        BigDecimal milestoneTotal = BigDecimal.ZERO;

        for(BigDecimal  milestone : milestonesAmounts) {
            milestoneTotal = milestoneTotal.add(milestone);
        }

        if (!milestoneTotal.equals(job.amount())) {
            System.out.println("Milestone total: " + milestoneTotal);
            System.out.println("Job Amount: " + job.amount());
            return true;
        }
        return false;
    }

    /**
     * Checks if a job exists in the repository by its unique identifier.
     *
     * @param jobId the unique identifier of the job to be checked
     * @return the job object if it exists in the repository
     * @throws RuntimeException if no job is found with the given identifier
     */
    public Jobs checkJobExists(@Valid UUID jobId) {
        Jobs job;
        job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found!"));

        return job;
    }
}
