package com.trustbridge.Domain.Repositories;

import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Features.Dashboard.Freelancer.Dto.EarningDataPointDto;
import com.trustbridge.Features.Dashboard.Freelancer.Dto.FinancialMetricsDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestones, UUID> {
    Optional<Milestones> findByJobId(UUID jobId);
    List<Milestones> findAllByJobId(UUID jobId);

    @Query("SELECT m FROM Milestones m WHERE m.job.id = :jobId AND m.status = 'IN_PROGRESS'")
    Optional<Milestones> findActiveMilestoneByJobId(UUID jobId);

    @Query(value = """
        SELECT 
            CAST(EXTRACT(MONTH FROM m.updated_at) AS INTEGER) AS ledgerMonth,
            CAST(EXTRACT(YEAR FROM m.updated_at) AS INTEGER) AS ledgerYear,
            SUM(m.amount) AS total
        FROM milestones m
        JOIN jobs j ON m.job_id = j.id 
        WHERE j.freelancer_id = :freelancerId 
          AND m.status = 'PAID_OUT' 
          AND m.updated_at >= CURRENT_DATE - INTERVAL '12 months'
        GROUP BY EXTRACT(YEAR FROM m.updated_at), EXTRACT(MONTH FROM m.updated_at)
        ORDER BY ledgerYear ASC, ledgerMonth ASC
        """, nativeQuery = true)
    List<EarningDataPointDto> getEarningsLast12Months(@Param("freelancerId") UUID freelancerId);

    @Query("""
        SELECT new com.trustbridge.Features.Dashboard.Freelancer.Dto.FinancialMetricsDto(
            COALESCE(SUM(CASE WHEN m.status IN ('AWAITING_PAYMENT', 'IN_PROGRESS', 'SUBMITTED', 'APPROVED', 'DISPUTE_NEGOTIATION', 'DISPUTE_ARBITRATION') THEN m.amount ELSE 0.0 END), 0.0),
            COALESCE(SUM(CASE WHEN m.status IN ('SUBMITTED', 'APPROVED') THEN m.amount ELSE 0.0 END), 0.0),
            COALESCE(SUM(CASE WHEN m.status = 'PAID_OUT' 
                AND EXTRACT(MONTH FROM m.updatedAt) = EXTRACT(MONTH FROM CURRENT_DATE) 
                AND EXTRACT(YEAR FROM m.updatedAt) = EXTRACT(YEAR FROM CURRENT_DATE) 
                THEN m.amount ELSE 0.0 END), 0.0)
        )
        FROM Milestones m
        WHERE m.job.freelancer.id = :freelancerId
    """)
    FinancialMetricsDto getFreelancerFinancialMetrics(@Param("freelancerId") UUID freelancerId);

    List<Milestones> findTop10ByJobFreelancerIdOrderByUpdatedAtDesc(UUID freelancerId);
}
