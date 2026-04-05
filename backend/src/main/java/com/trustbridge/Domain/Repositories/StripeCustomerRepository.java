package com.trustbridge.Domain.Repositories;

import com.trustbridge.Domain.Entities.StripeCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StripeCustomerRepository extends JpaRepository<StripeCustomer, UUID> {
    Optional<StripeCustomer> findByUserId(UUID userId);
}
