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
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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

        sm.stopReactively();

        sm.getStateMachineAccessor().doWithAllRegions(accessor -> {

            accessor.addStateMachineInterceptor(jobInterceptor);

            accessor.resetStateMachineReactively(new DefaultStateMachineContext<>(
                    job.getStatus(),null,null,null
            ));
        });

        sm.startReactively();

        return sm;
    }

    public void fireEvent(UUID jobId, jobEvent event) {
        StateMachine<jobStatus, jobEvent> sm = buildStateMachine(jobId);

        Message<jobEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader("jobId", jobId)
                .build();

        sm.sendEvent(Mono.just(message)).subscribe();
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

    public void pendingToActive(UUID jobId) {
        fireEvent(jobId, jobEvent.ACCEPT_OFFER);
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