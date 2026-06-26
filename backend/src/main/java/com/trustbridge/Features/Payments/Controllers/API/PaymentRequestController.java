package com.trustbridge.Features.Payments.Controllers.API;

import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Features.Payments.Dto.PaymentRequestResponse;
import com.trustbridge.Features.Payments.Service.PaymentRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment-requests")
@RequiredArgsConstructor
public class PaymentRequestController {

    private final PaymentRequestService paymentRequestService;
    private final MilestoneRepository milestoneRepository;

    // TODO: change payment intent so that it is created when milestone is created
    @PostMapping("/initiate")
    public ResponseEntity<PaymentRequestResponse> initiatePayment(@RequestBody Map<String, String> payload) {
        try {
            UUID milestoneId = UUID.fromString(payload.get("milestoneId"));

            Milestones milestones = milestoneRepository.findById(milestoneId)
                    .orElseThrow(() -> new RuntimeException("Milestone not found!"));

//            PaymentRequestResponse response = paymentRequestService.createPaymentRequest(milestones);
            System.out.println("ACTION FIRED: Payment Request created for Milestone " + milestoneId);
//            return ResponseEntity.ok(response);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}