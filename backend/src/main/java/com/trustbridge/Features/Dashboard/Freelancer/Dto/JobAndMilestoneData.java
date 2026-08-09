package com.trustbridge.Features.Dashboard.Freelancer.Dto;

import java.util.List;

public record JobAndMilestoneData(
   JobSummaryDto jobSummaryDto,
   List<MilestoneSummaryDto> milestoneSummaries
) {}
