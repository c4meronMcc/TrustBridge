package com.trustbridge.Features.Disputes.Service;

import com.trustbridge.Domain.Repositories.DisputeRepository;
import com.trustbridge.Features.Disputes.Dto.DisputeCreationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final DisputeStateService disputeStateService;

    public void createNewDispute(DisputeCreationDto dto, String authenticatedEmail){



    }

}
