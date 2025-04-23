package com.savorybites.backend.api.admin;

import com.savorybites.backend.api.dto.user.UserDTO;
import com.savorybites.backend.model.Admin;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.Status;
import com.savorybites.backend.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {


    private final AdminService adminService;

    public AdminUserController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/")
    public List<UserDTO> getUsers(@AuthenticationPrincipal Admin admin,
                                  @RequestParam(required = false) Status status) {
        List<LocalUser> userList;
        if(status==null)
            userList = adminService.getAllUsers(admin);
        else
            userList = adminService.getUsersByStatus(admin,status);
        return userList.stream().map(UserDTO::new).toList();
    }


    @PutMapping("/{userId}/toggleBan")
    public ResponseEntity<String> toggleUserBan(@AuthenticationPrincipal Admin admin,
                                          @PathVariable Long userId) {
        return adminService.toggleUserBan(admin, userId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal Admin admin,
                                             @PathVariable Long userId) {
        return adminService.deleteUser(admin, userId);
    }

    @GetMapping("/{userID}")
    public UserDTO getOrder(@AuthenticationPrincipal Admin admin,
                             @PathVariable Long userID) {
        return new UserDTO(adminService.getUser(admin, userID));
    }
}
