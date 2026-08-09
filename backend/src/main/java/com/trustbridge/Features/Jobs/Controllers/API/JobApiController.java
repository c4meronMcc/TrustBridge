package com.trustbridge.Features.Jobs.Controllers.API;

import ch.qos.logback.core.model.Model;
import com.trustbridge.Domain.Entities.Jobs;
import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Enums.UserRole.role;
import com.trustbridge.Domain.Repositories.JobRepository;
import com.trustbridge.Features.Jobs.Dto.JobCreationDto;
import com.trustbridge.Features.Jobs.Service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
public class JobApiController {

    JobRepository jobRepository;
    JobService jobService;

    /**
     * Creates a new job based on the provided details and associates it with the authenticated user.
     *
     * @param dto the job creation details including title, description, milestones, and client information
     * @param principal the security principal containing the authenticated user's information
     * @return a response entity containing a success message when the job has been created successfully
     */
    @PostMapping("/creation")
    public ResponseEntity<String> jobCreation(@RequestBody @Valid JobCreationDto dto, Principal principal) {

        String authenticatedEmail = principal.getName();

        jobService.processNewJobOffer(dto, authenticatedEmail);
        return  ResponseEntity.ok("Job created successfully!");
    }

    /**
     * Handles an invitation link for a job and returns relevant information about the job and the client.
     *
     * @param token the unique invitation token associated with the job
     * @param model the model object for storing attributes (not utilized in this method)
     * @return a ResponseEntity containing a response map with job details, freelancer information,
     *         client email, and status indicating if the client is an existing user or newly invited
     */
    @PostMapping("/invite/{token}")
    public ResponseEntity<?> handleInviteLink(@PathVariable String token, Model model) {
        Jobs job = jobRepository.findByInviteToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid Token"));

        Users client = job.getClient();

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("jobTitle", job.getTitle());
        response.put("jobDescription", job.getDescription());
        response.put("freelancerName", job.getFreelancer().getFirstName() + " " +  job.getFreelancer().getLastName());

        if (client != null && client.getUserRole() != role.CLIENT_GUEST) {
            response.put("status", "EXISTING_USER");
            response.put("email", client.getEmail());
        } else {
            response.put("status", "INVITED");
            response.put("email", client != null ? client.getEmail() : null);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Changes the status of a job to "Cancelled" based on the provided job ID.
     *
     * @param jobId the unique identifier of the job to be cancelled
     * @return a response entity containing a success message indicating the job has been cancelled
     */
    @PostMapping("/status/cancelled/{jobId}")
    public ResponseEntity<?> ChangeStatusToCancelled(@PathVariable String jobId) {
        jobService.jobStatusToCancelled(UUID.fromString(jobId));
        return ResponseEntity.ok("Job cancelled successfully!");
    }

    /**
     * Changes the status of a job to "Pending Acceptance" based on the provided job ID.
     *
     * @param jobId the unique identifier of the job whose status is to be changed to pending
     * @return a response entity containing a success message indicating the job status has been updated successfully
     */
    @PostMapping("/status/pending/{jobId}")
    public ResponseEntity<?> ChangeStatusToPending(@PathVariable String jobId) {
        jobService.jobStatusToPendingAccepted(UUID.fromString(jobId));
        return ResponseEntity.ok("Job pending successfully!");
    }

    /**
     * Changes the status of a job to "Active" based on the provided job ID.
     *
     * @param jobId the unique identifier of the job whose status is to be changed to active
     * @return a response entity containing a success message indicating the job's status has been updated successfully
     */
    @PostMapping("/status/Active/{jobId}")
    public ResponseEntity<?> ChangeStatusToActive(@PathVariable String jobId) {
        jobService.jobStatusToActive(UUID.fromString(jobId));
        return ResponseEntity.ok("Job active successfully!");
    }
}
