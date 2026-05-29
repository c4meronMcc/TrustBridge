package com.trustbridge.Features.Dashboard.Freelancer.Dto;

public record JobSummaryDto(
        /* Job */
        String jobId,
        String title,
        String clientName,
        double totalJobAmount,
        int progressPercentage,
        String status,

        /* Milestone */
        String currentMilestoneTitle,
//        String milestoneDescription,
        String DepositStatus,
        String deadline
) {}
