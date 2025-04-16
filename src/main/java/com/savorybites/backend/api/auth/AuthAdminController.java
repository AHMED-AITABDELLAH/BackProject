package com.savorybites.backend.api.auth;

import com.savorybites.backend.api.dto.auth.LoginBody;
import com.savorybites.backend.api.dto.auth.LoginResponse;
import com.savorybites.backend.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/admin")
public class AuthAdminController {


    private final AdminService adminService;

    public AuthAdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginBody loginBody, HttpServletRequest request){
        return adminService.login(loginBody,request);
    }
}
