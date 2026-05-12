package com.trustbridge.Domain.Repositories;


import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Entities.PaymentRequest;
import com.trustbridge.Domain.Enums.PaymentRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, UUID> {
    Optional<PaymentRequest> findByMilestone(Milestones milestones);
    Optional<PaymentRequest> findByPaymentLinkToken(String token);
    Optional<PaymentRequest> findByMilestoneIdAndStatus(UUID milestoneId, PaymentRequestStatus status);
}
