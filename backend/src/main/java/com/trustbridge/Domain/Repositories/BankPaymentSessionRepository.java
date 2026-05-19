package com.trustbridge.Domain.Repositories;

import com.trustbridge.Domain.Entities.BankPaymentSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankPaymentSessionRepository extends JpaRepository<BankPaymentSession, UUID> {
    Optional<BankPaymentSession> findByPaymentRequestId(UUID paymentRequestId);
}
