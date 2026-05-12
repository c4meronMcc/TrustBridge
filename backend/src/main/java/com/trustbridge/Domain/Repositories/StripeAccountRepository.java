package com.trustbridge.Domain.Repositories;

import com.trustbridge.Domain.Entities.StripeAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StripeAccountRepository extends JpaRepository<StripeAccount, UUID> {
    Optional<StripeAccount> findByUserId(UUID userId);
    Optional<Boolean> existsByUserId(UUID userId);
    Optional<String> findCustomerIdByUserId(UUID userId);
}
