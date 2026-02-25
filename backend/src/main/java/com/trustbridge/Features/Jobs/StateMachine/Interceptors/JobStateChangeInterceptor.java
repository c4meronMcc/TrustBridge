package com.trustbridge.Features.Jobs.StateMachine.Interceptors;

import com.trustbridge.Domain.Entities.Jobs;
import com.trustbridge.Domain.Repositories.JobRepository;
import com.trustbridge.Domain.Enums.JobStatus.*;
import com.trustbridge.Domain.Enums.JobEvent.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;


@Component
public class JobStateChangeInterceptor extends StateMachineInterceptorAdapter<jobStatus, jobEvent> {

    @Autowired
    JobRepository jobRepository;

    @Override
    public void preStateChange(State<jobStatus, jobEvent> state,
                               Message<jobEvent> message,
                               Transition<jobStatus, jobEvent> transition,
                               StateMachine<jobStatus, jobEvent> stateMachine,
                               StateMachine<jobStatus, jobEvent> rootStateMachine) {

        Optional.ofNullable(message).ifPresent(msg -> {

            UUID jobId = (UUID) msg.getHeaders().get("jobId");

            if (jobId != null) {
                Jobs job = jobRepository.findById(jobId)
                        .orElseThrow(() -> new RuntimeException("Job not found!"));

                String initialState = job.getStatus().name();

                job.setStatus(jobStatus.valueOf(state.getId().name()));

                jobRepository.save(job);

                System.out.println("Job Status Changed From: " + initialState + " to: " + state.getId().name());
            }
        });
    }
}
