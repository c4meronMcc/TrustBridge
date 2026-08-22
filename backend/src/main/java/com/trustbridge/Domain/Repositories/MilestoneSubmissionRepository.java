package com.trustbridge.Domain.Repositories;

import com.trustbridge.Domain.Entities.MilestoneSubmissions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MilestoneSubmissionRepository extends JpaRepository<MilestoneSubmissions, UUID> {
    Optional<MilestoneSubmissions> findByMilestoneIdOrderBySubmittedByDesc(UUID milestoneId);
    Optional<MilestoneSubmissions> findTopByMilestoneIdOrderByCreatedAtDesc(UUID milestoneId);
}

