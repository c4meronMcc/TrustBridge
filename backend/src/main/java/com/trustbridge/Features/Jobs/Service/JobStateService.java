package com.trustbridge.Features.Jobs.Service;

import com.trustbridge.Domain.Entities.Jobs;
import com.trustbridge.Domain.Repositories.JobRepository;
import com.trustbridge.Domain.Enums.JobStatus.*;
import com.trustbridge.Domain.Enums.JobEvent.*;
import com.trustbridge.Features.Jobs.StateMachine.Interceptors.JobStateChangeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
public class JobStateService {

    JobRepository jobRepository;

    @Autowired
    StateMachineFactory<jobStatus, jobEvent> stateMachineFactory;

    @Autowired
    JobStateChangeInterceptor jobInterceptor;

    public JobStateService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    private StateMachine<jobStatus, jobEvent>  buildStateMachine(UUID jobId) {

        Jobs job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found!"));

        StateMachine<jobStatus, jobEvent> sm = stateMachineFactory.getStateMachine(jobId.toString());

        sm.stopReactively().block();

        sm.getStateMachineAccessor().doWithAllRegions(accessor -> {

            accessor.addStateMachineInterceptor(jobInterceptor);

            accessor.resetStateMachineReactively(new DefaultStateMachineContext<>(
                    job.getStatus(),null,null,null
            )).block();
        });

        sm.startReactively().block();

        return sm;
    }

    public void fireEvent(UUID jobId, jobEvent event, Map<String, Object> additionalHeaders) {
        StateMachine<jobStatus, jobEvent> sm = buildStateMachine(jobId);

        // 1. Build the base message with the required ID
        MessageBuilder<jobEvent> builder = MessageBuilder
                .withPayload(event)
                .setHeader("jobId", jobId);

        // 2. Dynamically inject any extra data the specific state requires!
        if (additionalHeaders != null && !additionalHeaders.isEmpty()) {
            builder.copyHeaders(additionalHeaders);
        }

        // 3. Fire it safely and capture the Result Object
        var result = sm.sendEvent(Mono.just(builder.build())).blockLast();

        // 4. Extract the boolean by checking the ResultType
        boolean accepted = result != null && result.getResultType() == StateMachineEventResult.ResultType.ACCEPTED;

        if (!accepted) {
            throw new IllegalStateException("Guard blocked the transition for event: " + event);
        }
    }

    // The Convenience Wrapper (Keeps your old code from breaking!)
    public void fireEvent(UUID jobId, jobEvent event) {
        this.fireEvent(jobId, event, null);
    }

    // UN-GUARDED STATE TRANSITIONS
    public void draftToPending(UUID jobId) {
        fireEvent(jobId, jobEvent.PUBLISH_OFFER);
    }

    public void draftToCancelled(UUID jobId) {
        fireEvent(jobId, jobEvent.CANCEL_JOB);
    }

    public void pendingToCancelled(UUID jobId) {
        fireEvent(jobId, jobEvent.CANCEL_JOB);
    }

    public void pendingToActive(UUID jobId, String inviteToken) {
        fireEvent(jobId, jobEvent.ACCEPT_OFFER, Map.of("inviteToken", inviteToken));
    }

    public void submissionRevoked(UUID jobId) {
        fireEvent(jobId, jobEvent.SUBMISSION_REVOKED);
    }

    // GUARDED STATE TRANSITIONS
    public void offerAccepted(UUID jobId) {
        fireEvent(jobId, jobEvent.ACCEPT_OFFER);
    }

    public void fundsDeposited(UUID jobId) {
        fireEvent(jobId, jobEvent.FUNDS_DEPOSITED);
    }

    public void allMilestonesCompleted(UUID jobId) {
        fireEvent(jobId, jobEvent.ALL_MILESTONES_COMPLETED);
    }

    public void jobApproved(UUID jobId) {
        fireEvent(jobId, jobEvent.JOB_APPROVED);
    }

    public void releasePayment(UUID jobId) {
        fireEvent(jobId, jobEvent.RELEASE_PAYMENT);
    }

    public void raiseDispute(UUID jobId) {
        fireEvent(jobId, jobEvent.RAISE_DISPUTE);
    }

    public void resolveDispute(UUID jobId) {
        fireEvent(jobId, jobEvent.RESOLVE_DISPUTE);
    }
}