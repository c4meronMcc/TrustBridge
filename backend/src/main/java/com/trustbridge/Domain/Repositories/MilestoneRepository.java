package com.trustbridge.Domain.Repositories;

import com.trustbridge.Domain.Entities.Milestones;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MilestoneRepository extends JpaRepository<Milestones, UUID> {
    Optional<Milestones> findByJobId(UUID jobId);
    List<Milestones> findAllByJobId(UUID jobId);

}
