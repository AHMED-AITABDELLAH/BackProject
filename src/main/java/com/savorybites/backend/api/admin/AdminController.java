package com.savorybites.backend.api.admin;
import com.savorybites.backend.api.dto.admin.DashboardStatistics;
import com.savorybites.backend.model.*;
import com.savorybites.backend.service.AdminService;
import com.savorybites.backend.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin")
public class AdminController {

    private final DashboardService dashboardService;

    public AdminController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/accountStatus")
    public Status[] getAllAccountStatuses() {
        return Status.values();
    }

    @GetMapping("/dashboard")
    public DashboardStatistics approveRestaurant(@AuthenticationPrincipal Admin admin) {
        return dashboardService.getDashboardStats(admin);
    }


}