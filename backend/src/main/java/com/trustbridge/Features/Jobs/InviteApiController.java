package com.trustbridge.Features.Jobs;

import com.trustbridge.Domain.Entities.Jobs;
import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Enums.UserRole.role;
import com.trustbridge.Domain.Repositories.JobRepository;
import com.trustbridge.Features.Jobs.Dto.InviteResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invite")
public class InviteApiController {

    private final JobRepository jobRepository;

    public InviteApiController(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @GetMapping("/{token}")
    public ResponseEntity<InviteResponseDto> getInviteDetails(@PathVariable String token) {
        Jobs job = jobRepository.findByInviteToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        Users client = job.getClient();

        String status = (client != null && client.getUserRole() != role.CLIENT_GUEST)
                ? "EXISTING"
                : "NEW";

        InviteResponseDto response = new InviteResponseDto(
                job.getTitle(),
                job.getDescription(),
                job.getFreelancer().getFirstName() + " " + job.getFreelancer().getLastName(),
                job.getFreelancer().getEmail(),
                job.getTotalAmount(),
                job.getCurrency(),
                job.getClient().getEmail(),
                status
        );

        return ResponseEntity.ok(response);
    }

}
