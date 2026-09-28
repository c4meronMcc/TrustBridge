package com.trustbridge.Features.Disputes.Service;

import com.trustbridge.Domain.Repositories.DisputeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final DisputeStateService disputeStateService;

    public void createNewDispute(){

    }

}
