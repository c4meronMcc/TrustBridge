package com.trustbridge.Domain.Repositories;


import com.trustbridge.Domain.Entities.PaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, UUID> {
}
