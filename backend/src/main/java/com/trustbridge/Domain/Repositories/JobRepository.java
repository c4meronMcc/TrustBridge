package com.trustbridge.Domain.Repositories;

import com.trustbridge.Domain.Entities.Jobs;
import com.trustbridge.Features.Dashboard.Freelancer.Dto.JobSummaryDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Jobs, UUID> {

    Optional<Jobs> findByInviteToken(String inviteToken);

    @Query("""
        SELECT new com.trustbridge.Features.Dashboard.Freelancer.Dto.JobSummaryDto(
            CAST(j.id AS string),
            j.title,
            concat(c.firstName, ' ', c.lastName),
            CAST(j.totalAmount AS double),
            50, 
            CAST(j.status AS string),
            m.title,
            COALESCE(CAST(pr.status AS string), 'AWAITING_DEPOSIT'), 
            CAST(m.createdAt AS string)
        )
        FROM Jobs j
        JOIN j.client c
        JOIN Milestones m ON m.job.id = j.id
        LEFT JOIN PaymentRequest pr ON pr.milestone = m
        WHERE j.freelancer.id = :freelancerId 
          AND j.status = 'IN_PROGRESS'
          AND m.status IN ('IN_PROGRESS', 'SUBMITTED', 'APPROVED', 'DISPUTE_NEGOTIATION', 'DISPUTE_ARBITRATION')
        ORDER BY j.updatedAt DESC
        """)
    ArrayList<JobSummaryDto> findActiveJobByUserId(@Param("freelancerId") UUID freelancerId);

    @Query("""
        SELECT new com.trustbridge.Features.Dashboard.Freelancer.Dto.JobSummaryDto(
            CAST(j.id AS string),
            j.title,
            concat(c.firstName, ' ', c.lastName),
            CAST(j.totalAmount AS double),
            50, 
            CAST(j.status AS string),
            COALESCE(m.title, 'No active milestone'),
            COALESCE(CAST(pr.status AS string), 'N/A'),
            CAST(j.updatedAt AS string)
        )
        FROM Jobs j
        JOIN j.client c
        LEFT JOIN Milestones m ON m.job.id = j.id AND m.status IN ('IN_PROGRESS', 'SUBMITTED', 'APPROVED', 'DISPUTE_NEGOTIATION', 'DISPUTE_ARBITRATION')
        LEFT JOIN PaymentRequest pr ON pr.milestone = m
        WHERE j.freelancer.id = :freelancerId
        ORDER BY j.updatedAt DESC
    """)
    ArrayList<JobSummaryDto> findRecentActivityByFreelancerId(UUID freelancerId);

    @Query("""
        SELECT new com.trustbridge.Features.Dashboard.Freelancer.Dto.JobSummaryDto(
            CAST(j.id AS string),
            j.title,
            concat(c.firstName, ' ', c.lastName),
            CAST(j.totalAmount AS double),
            50, 
            CAST(j.status AS string),
            COALESCE(m.title, 'Initial Setup'),
            COALESCE(CAST(pr.status AS string), 'AWAITING_DEPOSIT'),
            CAST(j.createdAt AS string)
        )
        FROM Jobs j
        JOIN j.client c
        LEFT JOIN Milestones m ON m.job.id = j.id AND m.status IN ('PENDING', 'IN_PROGRESS')
        LEFT JOIN PaymentRequest pr ON pr.milestone = m
        WHERE j.freelancer.id = :freelancerId 
          AND j.status = 'AWAITING_PAYMENT'
        ORDER BY j.updatedAt DESC
    """)
    ArrayList<JobSummaryDto> findAllJobsAwaitingPayment(UUID freelancerId);
}
