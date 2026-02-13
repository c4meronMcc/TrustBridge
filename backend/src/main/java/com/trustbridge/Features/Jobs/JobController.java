package com.trustbridge.Features.Jobs;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RestController("/job")
public class JobController {

    @PostMapping("/creation")
    public String jobCreation() {
        return "Job/jobCreation.html";
    }

}
