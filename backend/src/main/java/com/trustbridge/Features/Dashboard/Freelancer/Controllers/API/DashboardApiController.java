package com.trustbridge.Features.Dashboard.Freelancer.Controllers.API;

import com.trustbridge.Features.Dashboard.Freelancer.Dto.DashboardDataDto;
import com.trustbridge.Features.Dashboard.Freelancer.Service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {

    private final DashboardService dashboardService;

    @GetMapping("/freelancer")
    public ResponseEntity<DashboardDataDto> getDashboardData(Authentication authentication) {

        // 1. Securely extract the email from the validated JWT token
        String email = authentication.getName();

        // 2. Fetch the data
        DashboardDataDto dashboardData = dashboardService.getDashboardData(email);

        // 3. Return the actual JSON payload
        return ResponseEntity.ok(dashboardData);
    }
}