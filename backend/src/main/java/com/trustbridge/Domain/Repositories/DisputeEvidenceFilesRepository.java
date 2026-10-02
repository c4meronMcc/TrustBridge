package com.trustbridge.Domain.Repositories;

import com.trustbridge.Domain.Entities.DisputeEvidenceFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DisputeEvidenceFilesRepository extends JpaRepository<DisputeEvidenceFiles, UUID> {

}
