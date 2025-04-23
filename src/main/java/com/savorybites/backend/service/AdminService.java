package com.savorybites.backend.service;


import com.savorybites.backend.api.dto.auth.LoginBody;
import com.savorybites.backend.api.dto.auth.LoginResponse;
import com.savorybites.backend.exception.BadAdminCredentialsException;
import com.savorybites.backend.exception.user.UserNotFoundException;
import com.savorybites.backend.model.*;
import com.savorybites.backend.model.verificationModels.VerificationToken;
import com.savorybites.backend.repository.*;
import com.savorybites.backend.repository.VerificationRepository.VerificationTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final OrdersRepository ordersRepository;

    public AdminService(LoginAttemptService loginAttemptService, AdminRepository adminRepository, EncryptionService encryptionService, JWTService jwtService, EmailService emailService, VerificationTokenRepository verificationTokenRepository, RestaurantRepository restaurantRepository, RestaurantService restaurantService, UserRepository userRepository, FoodRepository foodRepository, OrdersRepository ordersRepository) {
        this.loginAttemptService = loginAttemptService;
        this.adminRepository = adminRepository;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.restaurantRepository = restaurantRepository;
        this.restaurantService = restaurantService;
        this.userRepository = userRepository;
        this.foodRepository = foodRepository;
        this.ordersRepository = ordersRepository;
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


        Optional<Admin> optionalAdmin = adminRepository.findByEmail(loginBody.getEmail());
        if (optionalAdmin.isEmpty())
            throw new BadAdminCredentialsException(clientIP);

        Admin admin = optionalAdmin.get();
        boolean success =encryptionService.verifyPassword(loginBody.getPassword(),admin.getPassword());
                if(success){
                    loginAttemptService.loginSucceeded(clientIP);

                    return ResponseEntity.ok(new LoginResponse(jwtService.generateToken(admin)));

                }else
                    throw new BadAdminCredentialsException(clientIP);
    }

    private Admin verifyAdmin(Admin admin){
        return adminRepository.findById(admin.getId()).orElseThrow();
    }


    /// /////////Restaurant Logic

    public Restaurant getRestaurant(Admin admin, long restaurantID){
        Admin theAdmin = verifyAdmin(admin);
        return restaurantRepository.findById(restaurantID).orElseThrow();
    }

    public ResponseEntity<String> approveRestaurant(Admin admin, long restaurantID){

        Admin theAdmin =verifyAdmin(admin);

        Restaurant restaurant = restaurantRepository
                .findRestaurantById(restaurantID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));

        if (restaurant.getIsApproved())
            return ResponseEntity.badRequest().body("Restaurant already approved");

        restaurant.setIsApproved(true);
        restaurant.setStatus(Status.APPROVED);
        restaurantRepository.save(restaurant);

        // Now generate verification token and send email
        VerificationToken token = restaurantService.createVerificationToken(restaurant);
        verificationTokenRepository.save(token);
        emailService.sendRestaurantVerificationEmail(token);
        return ResponseEntity.ok("The Restaurant has been approved.");


    }

    public ResponseEntity<String> declineRestaurant(Admin admin, long restaurantID){

        Admin theAdmin =verifyAdmin(admin);

        Restaurant restaurant = restaurantRepository
                .findRestaurantById(restaurantID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));

        if (restaurant.getIsApproved())
            return ResponseEntity.badRequest().body("Restaurant already approved");

        restaurant.setIsApproved(false);
        restaurant.setStatus(Status.DECLINED);
        restaurantRepository.save(restaurant);

        // Now generate verification token and send email

        emailService.sendAccountIsNotApproved(restaurant);
        return ResponseEntity.ok("The Restaurant has been declined.");


    }

    public ResponseEntity<String> toggleRestaurantBan(Admin admin, long restaurantID){

        Admin theAdmin =verifyAdmin(admin);

        Restaurant restaurant = restaurantRepository
                .findRestaurantById(restaurantID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));


        if(restaurant.getStatus()!=Status.BANNED){
            restaurant.setStatus(Status.BANNED);
            restaurantRepository.save(restaurant);
            return ResponseEntity.ok("The Restaurant has been banned successfully.");
        }else{
            restaurant.setStatus(Status.VERIFIED);
            restaurantRepository.save(restaurant);
            return ResponseEntity.ok("The Restaurant has been unbanned successfully.");

        }




    }

    public ResponseEntity<String> deleteRestaurant(Admin admin, long restaurantID){

        Admin theAdmin =verifyAdmin(admin);

        Restaurant restaurant = restaurantRepository
                .findRestaurantById(restaurantID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));


       restaurantRepository.delete(restaurant);

        return ResponseEntity.ok("The Restaurant has been deleted.");


    }

    public List<Restaurant> getRestaurantByStatus(Admin admin,Status status){
        Admin theAdmin=verifyAdmin(admin);
        return restaurantRepository.findAllByStatus(status);
    }

    public List<Restaurant> getAllRestaurants(Admin admin){
        Admin theAdmin=verifyAdmin(admin);
        return restaurantRepository.findAll();
    }

    /// ///////Food Logic

    public List<Food> getAllFood(Admin admin){
        Admin theAdmin = verifyAdmin(admin);
        return foodRepository.findAll();
    }

    public List<Food> getAllFoodByStatus(Admin admin,FoodStatus foodStatus){
        Admin theAdmin = verifyAdmin(admin);
        return foodRepository.findAllByStatus(foodStatus);
    }

    public Food getFood(Admin admin, long foodID){
        Admin theAdmin = verifyAdmin(admin);
        return foodRepository.findById(foodID).orElseThrow();
    }

    public ResponseEntity<String> toggleFlagOnFood(Admin admin, long foodID){
        Admin theAdmin = verifyAdmin(admin);
        Optional<Food> optionalFood = foodRepository.findById(foodID);
        Food flageFood = optionalFood.orElseThrow();
        flageFood.setIsFlagged(!flageFood.getIsFlagged());
        if(flageFood.getIsFlagged()){
            flageFood.setStatus(FoodStatus.FLAGGED);
            foodRepository.save(flageFood);
            return ResponseEntity.ok("The food is flagged successfully.");
        }else{
            if(flageFood.getIsAvailable())
                flageFood.setStatus(FoodStatus.AVAILABLE);
            else
                flageFood.setStatus(FoodStatus.UNAVAILABLE);

            foodRepository.save(flageFood);
            return ResponseEntity.ok("The food is unflagged successfully.");
        }

    }

    /// ////////Users Logic

    public LocalUser getUser(Admin admin, long userID){
        Admin theAdmin = verifyAdmin(admin);
        return userRepository.findById(userID).orElseThrow();
    }

    public List<LocalUser> getAllUsers(Admin admin){
        Admin theAdmin = verifyAdmin(admin);
        return userRepository.findAll();
    }

    public List<LocalUser> getUsersByStatus(Admin admin,Status status){
        Admin theAdmin = verifyAdmin(admin);
        return userRepository.findAllByStatus(status);
    }

    @Transactional
    public ResponseEntity<String> toggleUserBan(Admin admin, long userID){

        Admin theAdmin =verifyAdmin(admin);

        LocalUser user = userRepository
                .findLocalUserById(userID)
                .orElseThrow(UserNotFoundException::new);

        if(user.getStatus()==Status.BANNED){
            user.setStatus(Status.VERIFIED);
            userRepository.save(user);
            return ResponseEntity.ok("The user has been unbanned successfully.");
        } else {
            user.setStatus(Status.BANNED);
            userRepository.save(user);

            return ResponseEntity.ok("The user has been banned.");
        }




    }

    public ResponseEntity<String> deleteUser(Admin admin, long userID){

        Admin theAdmin =verifyAdmin(admin);

        LocalUser user = userRepository
                .findLocalUserById(userID)
                .orElseThrow(UserNotFoundException::new);


        userRepository.delete(user);

        return ResponseEntity.ok("The user has been deleted.");


    }

    /// ///////Orders Logic

    public List<Orders> getAllOrders(Admin admin){
        Admin theAdmin = verifyAdmin(admin);
        return ordersRepository.findAll();
    }

    public Orders getOrder(Admin admin, long orderID){
        Admin theAdmin = verifyAdmin(admin);
        return ordersRepository.findById(orderID).orElseThrow();
    }

    public List<Orders> getAllOrdersByStatus(Admin admin, OrderStatus orderStatus) {
        Admin theAdmin = verifyAdmin(admin);
        return ordersRepository.findAllByStatus(orderStatus);
    }



}

