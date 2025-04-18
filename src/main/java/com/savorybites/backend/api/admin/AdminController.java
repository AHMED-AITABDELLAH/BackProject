package com.savorybites.backend.api.admin;
import com.savorybites.backend.api.dto.admin.DashboardStatistics;
import com.savorybites.backend.model.*;
import com.savorybites.backend.service.AdminService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/accountStatus")
    public Status[] getAllAccountStatuses() {
        return Status.values();
    }

    @GetMapping("/dashboard")
    public DashboardStatistics approveRestaurant(@AuthenticationPrincipal Admin admin) {
        return adminService.getDashboardStats(admin);
    }


}