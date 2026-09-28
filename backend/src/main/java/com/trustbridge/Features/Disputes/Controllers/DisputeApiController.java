package com.trustbridge.Features.Disputes.Controllers;

import com.trustbridge.Features.Jobs.Dto.JobCreationDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/disputes")
public class DisputeApiController {

    /**
     * Handles the request to open a new dispute.
     *
     * @return a ResponseEntity containing a success message indicating
     *         that the dispute has been successfully opened.
     */
    @PostMapping("/open-dispute")
    public ResponseEntity<String> openDispute(@RequestBody @Valid JobCreationDto dto, Principal principal) {

        String authenticatedEmail = principal.getName();

        return ResponseEntity.ok("Dispute Successfully Opened");
    }

}
