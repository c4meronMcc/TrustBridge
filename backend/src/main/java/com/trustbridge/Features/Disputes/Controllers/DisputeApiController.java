package com.trustbridge.Features.Disputes.Controllers;

import com.trustbridge.Features.Disputes.Dto.DisputeCreationDto;
import com.trustbridge.Features.Disputes.Dto.DisputeNegotiationStageFreelancerDto;
import com.trustbridge.Features.Disputes.Service.DisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/disputes")
public class DisputeApiController {

    private final DisputeService disputeService;

    /**
     * Handles the request to open a new dispute.
     *
     * @return a ResponseEntity containing a success message indicating
     *         that the dispute has been successfully opened.
     */
    @PostMapping(value = "/open-dispute", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> openDispute(
            @RequestPart("dto") @Valid DisputeCreationDto dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestHeader("X-Review-Token") String reviewToken
    ) throws IOException {

        disputeService.processClientDispute(dto);

        return ResponseEntity.ok("Dispute Successfully Opened");
    }


    /**
     * Retrieves the dispute submission details for a freelancer based on the provided invite token.
     *
     * @param inviteToken the unique token associated with the dispute submission.
     * @param principal the security context of the currently authenticated user.
     * @return a ResponseEntity containing the dispute submission details, wrapped in a
     *         DisputeNegotiationStageFreelancerDto object.
     */
    public ResponseEntity<DisputeNegotiationStageFreelancerDto> getDisputeSubmissionForFreelancer(String inviteToken, Principal principal) {

        DisputeNegotiationStageFreelancerDto dto = disputeService.getDisputeSubmissionForFreelancer(inviteToken);

        return ResponseEntity.ok(dto);
    }
}
