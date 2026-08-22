package com.trustbridge.Features.Jobs.Controllers.API;

import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Features.Jobs.Dto.MilestoneSubmission.MilestoneSubmissionReviewDto;
import com.trustbridge.Features.Jobs.Service.MilestoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/milestone")
@RequiredArgsConstructor
public class MilestoneApiController {

    private final MilestoneRepository milestoneRepository;
    private final MilestoneService milestoneService;

    @PostMapping(value = "/request-release/freelancer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> milestoneSubmissionFreelancer (
        @RequestParam("milestoneId") String milestoneId,
        @RequestParam(value = "deliverableLink", required = false) String deliverableLink,
        @RequestParam(value = "notes", required = false) String notes,
        @RequestParam(value = "scopeItems", required = false) String scopeItemsJson,
        @RequestParam(value = "files", required = false) List<MultipartFile> files) throws IOException {

        Milestones milestone = milestoneRepository.findById(UUID.fromString(milestoneId))
                .orElseThrow(() -> new RuntimeException("Milestone not found"));

        log.info("Milestone submission received for Milestone ID: {}", milestone.getId());

        milestoneService.freelancerSubmittedMilestone(UUID.fromString(milestoneId), deliverableLink, notes, scopeItemsJson, files);

        return ResponseEntity.ok("Submission received successfully.");
    }

    @GetMapping("/review/{milestoneId}")
    public ResponseEntity<MilestoneSubmissionReviewDto> getMilestoneSubmission(
            @PathVariable("milestoneId") UUID milestoneId) { // <-- Explicitly mapped
        return ResponseEntity.ok(milestoneService.getSubmissionForReview(milestoneId));
    }

}
