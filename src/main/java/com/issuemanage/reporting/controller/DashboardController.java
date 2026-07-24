package com.issuemanage.reporting.controller;

import com.issuemanage.reporting.dto.DashboardSummaryDTO;
import com.issuemanage.reporting.dto.WorkStatusDTO;
import com.issuemanage.reporting.service.DashboardService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEAM_LEAD', 'SUPPORT_ENGINEER', 'EMPLOYEE')")
    public ResponseEntity<DashboardSummaryDTO> getSummary(Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getSummary(authentication.getName()));
    }

    @GetMapping("/work-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEAM_LEAD')")
    public ResponseEntity<List<WorkStatusDTO>> getWorkStatus(Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getWorkStatus(authentication.getName()));
    }
}
