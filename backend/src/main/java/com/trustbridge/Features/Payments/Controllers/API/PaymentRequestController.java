package com.trustbridge.Features.Payments.Controllers.API;

import com.trustbridge.Domain.Entities.PaymentRequest;
import com.trustbridge.Features.Payments.Dto.PaymentRequestResponse;
import com.trustbridge.Features.Payments.Service.PaymentRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payment-requests")
public class PaymentRequestController {

    private final PaymentRequestService paymentRequestService;

    public PaymentRequestController(PaymentRequestService paymentRequestService) {
        this.paymentRequestService = paymentRequestService;
    }

    @PostMapping("/milestone/{milestoneId}")
    public ResponseEntity<?> createPaymentRequest(@PathVariable UUID milestoneId) {
        try {
            PaymentRequestResponse paymentRequest = paymentRequestService.createPaymentRequest(milestoneId);
            return ResponseEntity.ok(paymentRequest);
        } catch (Exception e) {
            e.printStackTrace(); // add this
            return ResponseEntity.badRequest().body("Failed to create payment request: " + e.getMessage());
        }
    }
}