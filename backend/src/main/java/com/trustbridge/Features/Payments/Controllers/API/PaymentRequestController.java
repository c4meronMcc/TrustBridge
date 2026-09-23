package com.trustbridge.Features.Payments.Controllers.API;

import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Entities.PaymentRequest;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Domain.Repositories.PaymentRequestRepository;
import com.trustbridge.Features.Jobs.Dto.MilestonePaymentDto;
import com.trustbridge.Features.Payments.Dto.PaymentRequestResponse;
import com.trustbridge.Features.Payments.Service.PaymentRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/payment-requests")
@RequiredArgsConstructor
public class PaymentRequestController {

    private final PaymentRequestService paymentRequestService;
    private final PaymentRequestRepository paymentRequestRepository;
    private final MilestoneRepository milestoneRepository;

    @GetMapping("/{paymentRequestId}")
    public ResponseEntity<MilestonePaymentDto> getPaymentRequest(@PathVariable("paymentRequestId") UUID paymentRequestId) {

        PaymentRequest paymentRequest = paymentRequestRepository.findById(paymentRequestId)
                .orElseThrow(() -> new RuntimeException("Payment not found!"));

        /*
        String jobTitle,
        String freelancerName,
        String clientName,
        String clientEmail,
        String currency,
        String milestoneTitle,
        String milestoneAmount,
        String clientSecret,
        String PaymentRequestId,
        String provider
        */


        return ResponseEntity.ok(new MilestonePaymentDto(
                paymentRequest.getMilestone().getJob().getTitle(),
                paymentRequest.getMilestone().getJob().getFreelancer().getFirstName() + " " + paymentRequest.getMilestone().getJob().getFreelancer().getLastName(),
                paymentRequest.getMilestone().getJob().getClient().getFirstName() + " " + paymentRequest.getMilestone().getJob().getClient().getLastName(),
                paymentRequest.getMilestone().getJob().getClient().getEmail(),
                paymentRequest.getMilestone().getJob().getCurrency(),
                paymentRequest.getMilestone().getTitle(),
                paymentRequest.getMilestone().getAmount().toString(),
                paymentRequest.getToken(),
                paymentRequestId.toString(),
                "mock"
        ));
    }

    @PostMapping("/initiate")
    public ResponseEntity<PaymentRequestResponse> initiatePayment(@RequestBody Map<String, String> payload) {
        try {
            UUID milestoneId = UUID.fromString(payload.get("milestoneId"));

            Milestones milestones = milestoneRepository.findById(milestoneId)
                    .orElseThrow(() -> new RuntimeException("Milestone not found!"));

//            PaymentRequestResponse response = paymentRequestService.createPaymentRequest(milestones);
            Logger.getLogger("ACTION FIRED: Payment Request created for Milestone " + milestoneId);
//            return ResponseEntity.ok(response);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}