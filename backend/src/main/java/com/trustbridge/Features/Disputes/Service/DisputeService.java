package com.trustbridge.Features.Disputes.Service;

import com.trustbridge.Domain.Entities.Dispute;
import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Repositories.DisputeRepository;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Domain.Repositories.UserRepository;
import com.trustbridge.Features.Disputes.Dto.DisputeCreationDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DisputeService {

    private final DisputeRepository disputeRepository;
//    private final DisputeStateService disputeStateService;
    private final UserRepository userRepository;
    private final MilestoneRepository milestoneRepository;

    public void processClientDispute(DisputeCreationDto dto, String authenicatedEmail) {

    }

    @Transactional
    public void createNewDispute(DisputeCreationDto dto) {

        Milestones milestone = milestoneRepository.findById(UUID.fromString(dto.milestoneId()))
                .orElseThrow(() -> new RuntimeException("Milestone not found"));

        Dispute newDispute = Dispute.builder()
                .milestone(milestone)
                .clientProposedAmount(dto.clientProposedAmount())
                .build();

        disputeRepository.save(newDispute);

    }

}
