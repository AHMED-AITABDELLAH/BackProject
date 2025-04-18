package com.savorybites.backend.api.dto.admin;

import com.savorybites.backend.model.Admin;
import com.savorybites.backend.model.Role;
import com.savorybites.backend.repository.AdminRepository;
import com.savorybites.backend.service.EncryptionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminDataSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final EncryptionService encryptionService;

    public AdminDataSeeder(AdminRepository adminRepository, EncryptionService encryptionService) {
        this.adminRepository = adminRepository;
        this.encryptionService = encryptionService;
    }

    @Override
    public void run(String... args) throws Exception {

        // Run only if there are no regions (i.e., no seed data yet)
        if (adminRepository.count() > 0) {
            System.out.println("Data already seeded, skipping...");
            return;
        }

        saveAdmin("6ahmedaitabdellah9@gmail.com",encryptionService.encryptPassword("Ahmed@SMI2025"));
        saveAdmin("asmaassoulaimani@gmail.com",encryptionService.encryptPassword("Asma@SMI2025"));
        saveAdmin("khadijaaithafid1@gmail.com",encryptionService.encryptPassword("Khadija@SMI2025"));

        System.out.println("3 default admins have been inserted.");
    }

    private void saveAdmin(String email,String password) {
            Admin admin = new Admin();
            admin.setEmail(email);
            admin.setPassword(password);
            admin.setRole(Role.ROLE_ADMIN);
            adminRepository.save(admin);
    }
}
