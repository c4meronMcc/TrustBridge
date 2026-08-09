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

    /**
     * Retrieves dashboard data for a freelancer based on their authenticated email.
     *
     * @param authentication the authentication object containing the information of the currently logged-in user.
     * @return a {@code ResponseEntity} containing the {@code DashboardDataDto} object with the freelancer's dashboard details.
     */
    @GetMapping("/freelancer")
    public ResponseEntity<DashboardDataDto> getDashboardData(Authentication authentication) {

        String email = authentication.getName();

        DashboardDataDto dashboardData = dashboardService.getDashboardData(email);

        return ResponseEntity.ok(dashboardData);
    }
}