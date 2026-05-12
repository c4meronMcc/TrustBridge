package com.trustbridge.Domain.Repositories;

import com.trustbridge.Domain.Entities.BankPaymentSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BankPaymentSessionRepository extends JpaRepository<BankPaymentSession, UUID> {
    Optional<BankPaymentSession> findByPaymentRequestId(UUID paymentRequestId);
}
