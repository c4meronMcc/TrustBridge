package com.trustbridge.Features.Payments.Controllers.API;

import com.trustbridge.Features.Payments.Dto.PaymentRequestResponse;
import com.trustbridge.Features.Payments.Service.PaymentRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment-requests")
public class PaymentRequestController {

    private final PaymentRequestService paymentRequestService;

    public PaymentRequestController(PaymentRequestService paymentRequestService) {
        this.paymentRequestService = paymentRequestService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<PaymentRequestResponse> initiatePayment(@RequestBody Map<String, String> payload) {
        try {
            UUID milestoneId = UUID.fromString(payload.get("milestoneId"));

            PaymentRequestResponse response = paymentRequestService.createPaymentRequest(milestoneId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}