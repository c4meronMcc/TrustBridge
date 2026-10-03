package com.trustbridge.Domain.Repositories;


import com.trustbridge.Domain.Entities.DisputeEvidenceSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DisputeEvidenceSubmissionRepository extends JpaRepository<DisputeEvidenceSubmission, UUID> {
    DisputeEvidenceSubmission findByDisputeId(UUID disputeId);
}
