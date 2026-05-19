package com.trustbridge.Domain.Repositories;

import com.trustbridge.Domain.Entities.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {
    EmailLog findByRecipientEmail(String email);
}
