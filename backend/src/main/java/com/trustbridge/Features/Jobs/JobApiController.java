package com.trustbridge.Features.Jobs;

import ch.qos.logback.core.model.Model;
import com.trustbridge.Domain.Entities.Jobs;
import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Enums.UserRole.role;
import com.trustbridge.Domain.Repositories.JobRepository;
import com.trustbridge.Features.Jobs.Dto.JobCreationDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/job")
public class JobApiController {

    JobRepository jobRepository;
    JobService jobService;

    JobApiController(JobRepository jobRepository,  JobService jobService) {
        this.jobRepository = jobRepository;
        this.jobService = jobService;
    }

    @PostMapping("/creation")
    public ResponseEntity<String> jobCreation(@RequestBody @Valid JobCreationDto dto) {
        jobService.processNewJobOffer(dto);
        return  ResponseEntity.ok("Job created successfully!");
    }

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

}
