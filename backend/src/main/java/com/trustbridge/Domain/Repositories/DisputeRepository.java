package com.trustbridge.Domain.Repositories;

import com.trustbridge.Domain.Entities.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DisputeRepository extends JpaRepository<Dispute, UUID> {
}
