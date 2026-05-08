package com.trustbridge.Domain.Repositories;

import com.trustbridge.Domain.Entities.StripeWebhookLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Book;
import java.util.UUID;

@Repository
public interface StripeWebhookLogsRepository extends JpaRepository<StripeWebhookLogs, UUID> {
    Boolean existsByStripeEventId(String stripeEventId);
}
