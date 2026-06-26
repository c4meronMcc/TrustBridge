package com.trustbridge.Features.Dashboard.Freelancer.Controllers.API;

import com.trustbridge.Features.Dashboard.Freelancer.Dto.JobAndMilestoneData;
import com.trustbridge.Features.Dashboard.Freelancer.Service.JobAndMilestoneSummaryService;
import lombok.RequiredArgsConstructor;
import okhttp3.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
public class JobAndMilestoneApiController {

    private final JobAndMilestoneSummaryService jobAndMilestoneSummaryService;

    @RequestMapping("/milestone-summary")
    public ResponseEntity<JobAndMilestoneData> getJobAndMilestoneSummary(Authentication authentication, @RequestParam("jobId") String jobId) {

        String email = authentication.getName();

        return ResponseEntity.ok(jobAndMilestoneSummaryService.getJobAndMilestoneSummary(email, jobId));
    }

}
