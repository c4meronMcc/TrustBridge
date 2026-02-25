package com.trustbridge.Domain.Repositories;


import com.trustbridge.Domain.Entities.PaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, UUID> {
    Optional<PaymentRequest> findByJobId(UUID jobId);
    Optional<PaymentRequest> findByPaymentId(String paymentId);
}
