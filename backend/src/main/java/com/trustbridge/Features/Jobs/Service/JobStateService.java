package com.trustbridge.Features.Jobs.Service;

import com.trustbridge.Domain.Entities.Jobs;
import com.trustbridge.Domain.Repositories.JobRepository;
import com.trustbridge.Domain.Enums.JobStatus.*;
import com.trustbridge.Domain.Enums.JobEvent.*;
import com.trustbridge.Features.Jobs.StateMachine.Interceptors.JobStateChangeInterceptor;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class JobStateService {

    private final JobRepository jobRepository;

    @Autowired
    StateMachineFactory<jobStatus, jobEvent> stateMachineFactory;

    @Autowired
    JobStateChangeInterceptor jobInterceptor;

    /**
     * Builds and initializes a state machine for the specified job.
     *
     * @param jobId the unique identifier of the job for which the state machine is being built
     * @return the initialized state machine for managing the job's state transitions
     * @throws RuntimeException if the job with the provided ID is not found
     */
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

    /**
     * Triggers a state transition event for the specified job using the state machine.
     * The method validates and sends an event to update the state of the job.
     *
     * @param jobId the unique identifier of the job for which the event is being triggered
     * @param event the state transition event to be triggered
     * @param additionalHeaders a map of additional headers to be dynamically injected into the event message,
     *                          can be null if no extra headers are required
     * @throws IllegalStateException if the event transition is not accepted by the state machine
     */
    public void fireEvent(UUID jobId, jobEvent event, Map<String, Object> additionalHeaders) {
        StateMachine<jobStatus, jobEvent> sm = buildStateMachine(jobId);

        MessageBuilder<jobEvent> builder = MessageBuilder
                .withPayload(event)
                .setHeader("jobId", jobId);

        if (additionalHeaders != null && !additionalHeaders.isEmpty()) {
            builder.copyHeaders(additionalHeaders);
        }

        var result = sm.sendEvent(Mono.just(builder.build())).blockLast();

        boolean accepted = result != null && result.getResultType() == StateMachineEventResult.ResultType.ACCEPTED;

        if (!accepted) {
            throw new IllegalStateException("Guard blocked the transition for event: " + event);
        }
    }

    /**
     * Triggers a state transition event for the specified job using the state machine.
     * This method serves as a convenience wrapper to fire an event without additional headers.
     *
     * @param jobId the unique identifier of the job for which the event is being triggered
     * @param event the state transition event to be triggered
     * @throws IllegalStateException if the event transition is not accepted by the state machine
     */
    public void fireEvent(UUID jobId, jobEvent event) {
        this.fireEvent(jobId, event, null);
    }

    /**
     * Transitions a job from the draft state to the pending state by triggering the appropriate state machine event.
     *
     * @param jobId the unique identifier of the job to be transitioned to the pending state
     * @throws IllegalStateException if the state transition is not accepted by the state machine
     */
    public void draftToPending(UUID jobId) {
        fireEvent(jobId, jobEvent.PUBLISH_OFFER);
    }

    /**
     * Transitions the state of a job identified by its UUID from "Draft" to "Cancelled".
     *
     * @param jobId the unique identifier of the job to be transitioned to the "Cancelled" state
     */
    public void draftToCancelled(UUID jobId) {
        fireEvent(jobId, jobEvent.CANCEL_JOB);
    }

    /**
     * Transitions a job from a pending state to a cancelled state.
     * This method triggers an event to notify the system of the job cancellation.
     *
     * @param jobId the unique identifier of the job to be transitioned to the cancelled state
     */
    public void pendingToCancelled(UUID jobId) {
        fireEvent(jobId, jobEvent.CANCEL_JOB);
    }

    /**
     * Transitions a job from the pending state to the active state by processing
     * the provided job ID and invite token.
     *
     * @param jobId       the unique identifier of the job to transition
     * @param inviteToken the invitation token associated with the job
     */
    public void pendingToActive(UUID jobId, String inviteToken) {
        fireEvent(jobId, jobEvent.ACCEPT_OFFER, Map.of("inviteToken", inviteToken));
    }

    /**
     * Handles the revocation of a submission for a specific job.
     * This method triggers an event to indicate that the submission has been revoked.
     *
     * @param jobId the unique identifier of the job for which the submission is being revoked
     */
    public void submissionRevoked(UUID jobId) {
        fireEvent(jobId, jobEvent.SUBMISSION_REVOKED);
    }

    /**
     * Handles the acceptance of an offer for the specified job by triggering the `ACCEPT_OFFER` event
     * on the job's state machine.
     *
     * @param jobId the unique identifier of the job for which the offer has been accepted
     * @throws IllegalStateException if the state transition is not accepted by the state machine
     */
    public void offerAccepted(UUID jobId) {
        fireEvent(jobId, jobEvent.ACCEPT_OFFER);
    }

    /**
     * Handles the state transition for a job when funds have been deposited.
     * Triggers the `FUNDS_DEPOSITED` event on the job's state machine.
     *
     * @param jobId the unique identifier of the job for which the funds have been deposited
     * @throws IllegalStateException if the state transition is not accepted by the state machine
     */
    public void fundsDeposited(UUID jobId) {
        fireEvent(jobId, jobEvent.FUNDS_DEPOSITED);
    }

    /**
     * Marks all milestones of a job as completed by triggering the appropriate state machine event.
     *
     * @param jobId the unique identifier of the job for which all milestones are being marked as completed
     * @throws IllegalStateException if the state transition is not accepted by the state machine
     */
    public void allMilestonesCompleted(UUID jobId) {
        fireEvent(jobId, jobEvent.ALL_MILESTONES_COMPLETED);
    }

    /**
     * Approves the specified job by triggering the appropriate state machine event.
     *
     * @param jobId the unique identifier of the job to be approved
     * @throws IllegalStateException if the state transition is not accepted by the state machine
     */
    public void jobApproved(UUID jobId) {
        fireEvent(jobId, jobEvent.JOB_APPROVED);
    }

    /**
     * Releases the payment for the specified job by triggering the appropriate state machine event.
     *
     * @param jobId the unique identifier of the job for which the payment is being released
     * @throws IllegalStateException if the state transition is not accepted by the state machine
     */
    public void releasePayment(UUID jobId) {
        fireEvent(jobId, jobEvent.RELEASE_PAYMENT);
    }

    /**
     * Initiates a dispute for the specified job by triggering the appropriate state transition event.
     *
     * @param jobId the unique identifier of the job for which the dispute is being raised
     * @throws IllegalStateException if the state transition is not accepted by the state machine
     */
    public void raiseDispute(UUID jobId) {
        fireEvent(jobId, jobEvent.RAISE_DISPUTE);
    }

    /**
     * Resolves a dispute for the specified job by triggering the appropriate state machine event.
     *
     * @param jobId the unique identifier of the job for which the dispute is being resolved
     * @throws IllegalStateException if the state transition is not accepted by the state machine
     */
    public void resolveDispute(UUID jobId) {
        fireEvent(jobId, jobEvent.RESOLVE_DISPUTE);
    }
}