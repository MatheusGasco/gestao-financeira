package com.gestaofinanceira.modules.dashboard;

import com.gestaofinanceira.modules.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardDTO.Summary> getSummary(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dashboardService.getSummary(user));
    }

    @GetMapping("/evolution")
    public ResponseEntity<List<DashboardDTO.MonthlyPoint>> getMonthlyEvolution(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dashboardService.getMonthlyEvolution(user));
    }
}
