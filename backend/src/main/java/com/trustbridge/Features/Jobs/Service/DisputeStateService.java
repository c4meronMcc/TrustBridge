package com.trustbridge.Features.Jobs.Service;

import com.trustbridge.Domain.Entities.Dispute;
import com.trustbridge.Domain.Enums.DisputeEvent;
import com.trustbridge.Domain.Enums.DisputeState;
import com.trustbridge.Domain.Repositories.DisputeRepository;
import com.trustbridge.Features.Jobs.StateMachine.Interceptors.DisputeStateChangeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class DisputeStateService {

    DisputeRepository disputeRepository;

    @Autowired
    StateMachineFactory<DisputeState, DisputeEvent> stateMachineFactory;

    @Autowired
    DisputeStateChangeInterceptor disputeInterceptor;

    public DisputeStateService(DisputeRepository disputeRepository) {
        this.disputeRepository = disputeRepository;
    }

    private StateMachine<DisputeState, DisputeEvent> buildStateMachine(UUID disputeId) {

        Dispute dispute = disputeRepository.findByJobId(disputeId)
                .orElseThrow(() -> new RuntimeException("Dispute not found!"));

        StateMachine<DisputeState, DisputeEvent> sm = stateMachineFactory.getStateMachine(disputeId.toString());

        sm.stopReactively().block();

        sm.getStateMachineAccessor().doWithAllRegions( accessor -> {

            accessor.addStateMachineInterceptor(disputeInterceptor);

            accessor.resetStateMachineReactively(new DefaultStateMachineContext<>(
                    dispute.getState(), null, null, null
            )).block();
        });
        sm.startReactively().block();

        return sm;
    }

    public void fireEvent(UUID disputeId, DisputeEvent event) {
        StateMachine<DisputeState, DisputeEvent> sm = buildStateMachine(disputeId);

        Message<DisputeEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader("disputeId", disputeId)
                .build();

        sm.sendEvent(Mono.just(message)).subscribe();
    }

    public void disputeRaised(UUID disputeId) {
        fireEvent(disputeId, DisputeEvent.OPEN_DISPUTE);
    }

    public void disputeEvidenceSubmitted(UUID disputeId) {
        fireEvent(disputeId, DisputeEvent.SUBMIT_EVIDENCE);
    }

    public void disputeProposalSubmitted(UUID disputeId) {
        fireEvent(disputeId, DisputeEvent.SUBMIT_PROPOSAL);
    }

    public void disputeProposalAccepted(UUID disputeId) {
        fireEvent(disputeId, DisputeEvent.ACCEPT_PROPOSAL);
    }

    public void disputeMiddleAccepted(UUID disputeId) {
        fireEvent(disputeId, DisputeEvent.ACCEPT_MIDDLE);
    }

    public void disputeProposalRejected(UUID disputeId) {
        fireEvent(disputeId, DisputeEvent.REJECT_PROPOSAL);
    }

    public void disputeEscalated(UUID disputeId) {
        fireEvent(disputeId, DisputeEvent.REJECT_PROPOSAL);
    }

    public void disputeAdminDecided(UUID disputeId) {
        fireEvent(disputeId, DisputeEvent.ADMIN_DECIDED);
    }
}