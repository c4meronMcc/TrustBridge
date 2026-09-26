package com.trustbridge.Features.Disputes.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/disputes")
public class DisputeApiController {

    @PostMapping("/open-dispute")
    public void openDispute() {

    }

}
