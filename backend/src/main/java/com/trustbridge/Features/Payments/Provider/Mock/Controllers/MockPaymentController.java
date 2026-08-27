package com.trustbridge.Features.Payments.Provider.Mock.Controllers;

import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Domain.Repositories.PaymentRequestRepository;
import com.trustbridge.Features.Payments.Events.MilestoneFundedEvent;
import com.trustbridge.Features.Payments.Service.PaymentStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Profile("mock")
@RequiredArgsConstructor
@RequestMapping("/api/v1/mock/payments")
public class MockPaymentController {


    private final PaymentStateService paymentStateService;
    private final MilestoneRepository milestoneRepository;
    private final PaymentRequestRepository paymentRequestRepository;

    @Autowired
    ApplicationEventPublisher eventPublisher;



    @PostMapping("/{paymentRequestId}/confirm")
    public ResponseEntity<?> confirmPayment(@PathVariable UUID paymentRequestId) {



        Milestones milestone = paymentRequestRepository.findById(paymentRequestId).get().getMilestone();

        // look up milestoneId from the PaymentRequest, then:
        paymentStateService.paymentSuccessful(paymentRequestId);
        eventPublisher.publishEvent(new MilestoneFundedEvent(this, milestone.getId()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{paymentRequestId}/fail")
    public ResponseEntity<?> failPayment(@PathVariable UUID paymentRequestId) {
        paymentStateService.paymentFailed(paymentRequestId);
        return ResponseEntity.ok().build();
    }

}
