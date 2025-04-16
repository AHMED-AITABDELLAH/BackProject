package com.savorybites.backend.service;


import com.savorybites.backend.api.dto.auth.LoginBody;
import com.savorybites.backend.api.dto.auth.LoginResponse;
import com.savorybites.backend.exception.BadAdminCredentialsException;
import com.savorybites.backend.model.Admin;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.model.verificationModels.VerificationToken;
import com.savorybites.backend.repository.AdminRepository;
import com.savorybites.backend.repository.RestaurantRepository;
import com.savorybites.backend.repository.VerificationRepository.VerificationTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class AdminService {

    private final LoginAttemptService loginAttemptService;
    private final AdminRepository adminRepository;
    private final EncryptionService encryptionService;
    private final JWTService jwtService;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    public final RestaurantRepository restaurantRepository;
    public final RestaurantService restaurantService;

    public AdminService(LoginAttemptService loginAttemptService, AdminRepository adminRepository, EncryptionService encryptionService, JWTService jwtService, EmailService emailService, VerificationTokenRepository verificationTokenRepository, RestaurantRepository restaurantRepository, RestaurantService restaurantService) {
        this.loginAttemptService = loginAttemptService;
        this.adminRepository = adminRepository;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.restaurantRepository = restaurantRepository;
        this.restaurantService = restaurantService;
    }

    public String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    public ResponseEntity<LoginResponse> login(LoginBody loginBody, HttpServletRequest request) {

        String clientIP =getClientIP(request);

        if (loginAttemptService.isBlocked(clientIP))
            throw new RequestRejectedException("Too many failed login attempts. Please try again later.");

        System.out.println("email : "+loginBody.getEmail() +"\n password : "+loginBody.getPassword());

        Optional<Admin> optionalAdmin = adminRepository.findByEmail(loginBody.getEmail());
        if (optionalAdmin.isEmpty())
            throw new BadAdminCredentialsException(clientIP);

        System.out.println("Admin is captured");
        Admin admin = optionalAdmin.get();
        boolean success =encryptionService.verifyPassword(loginBody.getPassword(),admin.getPassword());
        System.out.println("Admin is captured and suckseed is "+success);
                if(success){
                    loginAttemptService.loginSucceeded(clientIP);

                    return ResponseEntity.ok(new LoginResponse(jwtService.generateToken(admin)));

                }else
                    throw new BadAdminCredentialsException(clientIP);
    }

    private Admin verifyAdmin(Admin admin){
        return adminRepository.findById(admin.getId()).orElseThrow();
    }

    public ResponseEntity<String> approveRestaurant(Admin admin, long restaurantID){

        Admin theAdmin =verifyAdmin(admin);

        Restaurant restaurant = restaurantRepository
                .findRestaurantById(restaurantID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));

        if (restaurant.getIsApproved())
            return ResponseEntity.badRequest().body("Restaurant already approved");

        restaurant.setIsApproved(true);
        restaurantRepository.save(restaurant);

        // Now generate verification token and send email
        VerificationToken token = restaurantService.createVerificationToken(restaurant);
        verificationTokenRepository.save(token);
        emailService.sendRestaurantVerificationEmail(token);
        return ResponseEntity.ok("The Restaurant has been approved.");
    }
}
