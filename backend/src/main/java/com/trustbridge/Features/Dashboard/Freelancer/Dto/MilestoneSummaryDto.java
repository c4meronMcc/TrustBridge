package com.trustbridge.Features.Dashboard.Freelancer.Dto;

public record MilestoneSummaryDto(
   String milestoneId,
   int orderIndex,
   String title,
   double milestoneAmount,
   String status
) {}
