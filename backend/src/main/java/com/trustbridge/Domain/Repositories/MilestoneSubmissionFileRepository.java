package com.trustbridge.Domain.Repositories;

import com.trustbridge.Domain.Entities.MilestoneSubmissionFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MilestoneSubmissionFileRepository extends JpaRepository<MilestoneSubmissionFile, UUID> {

}
