package com.trustbridge.Features.Jobs.Controllers.API;

import com.trustbridge.Domain.Entities.Jobs;
import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Enums.UserRole.role;
import com.trustbridge.Domain.Repositories.JobRepository;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Features.Jobs.Dto.InviteResponseWebDto; // 🚨 Fixed typo and removed wildcard
import com.trustbridge.Features.Jobs.Dto.InviteMilestoneWebDto;
import com.trustbridge.Features.Jobs.Dto.PaymentActivationDto;
import com.trustbridge.Features.Jobs.Service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List; // 🚨 Added missing List import


@RestController
@RequestMapping("/api/invite")
@RequiredArgsConstructor
public class InviteApiController {

    private final JobRepository jobRepository;
    private final MilestoneRepository milestoneRepository;
    private final JobService jobService;

    @GetMapping("/{token}")
    // 🚨 Fixed return type to exactly match your DTO name
    public ResponseEntity<InviteResponseWebDto> getInviteDetails(@PathVariable String token) {
        Jobs job = jobRepository.findByInviteToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        List<Milestones> milestones = milestoneRepository.findAllByJobId(job.getId());

        Users client = job.getClient();

        String status = (client != null && client.getUserRole() != role.CLIENT_GUEST)
                ? "EXISTING"
                : "NEW";

        List<InviteMilestoneWebDto> milestoneDtos = milestones.stream()
                .map(m -> new InviteMilestoneWebDto(
                        m.getId().toString(),
                        m.getTitle(),
                        m.getAmount(),
                        m.getSequenceOrder()
                ))
                .toList();

        InviteResponseWebDto response = new InviteResponseWebDto(
                job.getTitle(),
                job.getDescription(),
                job.getClient().getFirstName(),
                job.getFreelancer().getFirstName() + " " + job.getFreelancer().getLastName(),
                job.getFreelancer().getEmail(),
                job.getTotalAmount(),
                job.getCurrency(),
                client != null ? client.getEmail() : null, // 🚨 Safely handled potential NullPointerException
                status,
                milestoneDtos
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/accepted/{token}")
    public ResponseEntity<PaymentActivationDto> acceptInvite(@PathVariable String token) {
        // Call the idempotent service method we just built
        PaymentActivationDto responseData = jobService.activateJob(token);

        // Spring Boot's Jackson library will automatically convert this Map
        // into valid JSON: {"clientSecret": "pi_123..."}
        return ResponseEntity.ok(responseData);
    }
}