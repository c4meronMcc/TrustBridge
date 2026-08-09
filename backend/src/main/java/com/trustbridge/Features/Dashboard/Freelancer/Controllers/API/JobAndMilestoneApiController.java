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

    /**
     * Retrieves a summary of the job and its related milestones for the authenticated user.
     *
     * @param authentication the authentication object containing the information of the currently logged-in user.
     * @param jobId the unique identifier of the job to retrieve the summary for.
     * @return a {@code ResponseEntity} containing a {@code JobAndMilestoneData} object with the job summary and milestone details.
     */
    @RequestMapping("/milestone-summary")
    public ResponseEntity<JobAndMilestoneData> getJobAndMilestoneSummary(Authentication authentication, @RequestParam("jobId") String jobId) {

        String email = authentication.getName();

        return ResponseEntity.ok(jobAndMilestoneSummaryService.getJobAndMilestoneSummary(email, jobId));
    }

}
