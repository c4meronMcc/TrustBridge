package com.trustbridge.Domain.Repositories;

import com.trustbridge.Domain.Entities.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {
    EmailLog findByEmail(String email);
}
