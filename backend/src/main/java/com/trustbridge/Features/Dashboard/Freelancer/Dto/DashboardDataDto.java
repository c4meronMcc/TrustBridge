package com.trustbridge.Features.Dashboard.Freelancer.Dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDataDto(
        String firstName,
        String lastName,
        Integer trustScore,
        BigDecimal fundsInEscrowHolding,
        BigDecimal fundsPending,
        BigDecimal fundsPaidOut,
        List<EarningDataPointDto> earningsChart,
        List<JobSummaryDto> activeJobs,
        List<JobSummaryDto> awaitingPaymentJobs,
        List<JobSummaryDto> recentlyCompletedJobs,
        List<AuditLogEntryDto> recentActivity
) {}