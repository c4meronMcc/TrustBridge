package com.trustbridge.Domain.Repositories;

import com.trustbridge.Domain.Entities.Jobs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Jobs, UUID> {

    Optional<Jobs> findByInviteToken(String inviteToken);

}
