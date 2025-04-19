package com.savorybites.backend.service;


import com.savorybites.backend.api.dto.admin.*;
import com.savorybites.backend.api.dto.auth.LoginBody;
import com.savorybites.backend.api.dto.auth.LoginResponse;
import com.savorybites.backend.exception.BadAdminCredentialsException;
import com.savorybites.backend.exception.user.UserNotFoundException;
import com.savorybites.backend.model.*;
import com.savorybites.backend.model.verificationModels.VerificationToken;
import com.savorybites.backend.repository.*;
import com.savorybites.backend.repository.VerificationRepository.VerificationTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
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

    public ResponseEntity<String> banRestaurant(Admin admin, long restaurantID){

        Admin theAdmin =verifyAdmin(admin);

        Restaurant restaurant = restaurantRepository
                .findRestaurantById(restaurantID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));


        restaurant.setStatus(Status.BANNED);
        restaurantRepository.save(restaurant);

        return ResponseEntity.ok("The Restaurant has been banned.");


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

    public ResponseEntity<String> banUser(Admin admin, long userID){

        Admin theAdmin =verifyAdmin(admin);

        LocalUser user = userRepository
                .findLocalUserById(userID)
                .orElseThrow(UserNotFoundException::new);


        user.setStatus(Status.BANNED);
        userRepository.save(user);

        return ResponseEntity.ok("The user has been banned.");


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



    /// ////Dashboard Statistics


    private FoodStatistics getFoodStatistics(){
        FoodStatistics foodStatistics = new FoodStatistics();
        foodStatistics.setTotalFoodCount(foodRepository.count());
        foodStatistics.setTotalFlaggedFood(foodRepository.countByIsFlaggedTrue());
        foodStatistics.setTotalUnflaggedFood();
        foodStatistics.setMostExpensiveFoodPrice(foodRepository.findMostExpensiveFoodPrice());
        foodStatistics.setLeastExpensiveFoodPrice(foodRepository.findLeastExpensiveFoodPrice());
        return foodStatistics;
    }

    private UserStatistics getUserStatistics(){
        UserStatistics userStatistics=new UserStatistics();
        userStatistics.setTotalUserCount(userRepository.count());
        userStatistics.setBannedUserCount(userRepository.countByStatus(Status.BANNED));
        userStatistics.setVerifiedUserCount(userRepository.countByStatus(Status.VERIFIED));
        return userStatistics;
    }

    private RestaurantStatistics getRestaurantStatistics(){
        RestaurantStatistics restaurantStatistics=new RestaurantStatistics();
        restaurantStatistics.setTotalRestaurantCount(restaurantRepository.count());
        restaurantStatistics.setBannedRestaurantCount(restaurantRepository.countByStatus(Status.BANNED));
        restaurantStatistics.setVerifiedRestaurantCount(restaurantRepository.countByStatus(Status.VERIFIED));
        restaurantStatistics.setDeclinedRestaurantCount(restaurantRepository.countByStatus(Status.DECLINED));
        restaurantStatistics.setApprovedRestaurantCount(restaurantRepository.countByStatus(Status.APPROVED));
        restaurantStatistics.setPendingRestaurantCount(restaurantRepository.countByStatus(Status.PENDING_APPROVAL));
        return restaurantStatistics;
    }

    private List<WeeklyOrderStats> getWeeklyOrderStats() {
        List<Object[]> rawData = ordersRepository.findWeeklyOrderStats();

        return rawData.stream().map(obj -> {
            WeeklyOrderStats dto = new WeeklyOrderStats();
            dto.setWeekLabel((String) obj[0]);
            dto.setDeliveredOrdersCount(((Number) obj[1]).intValue());
            dto.setCancelledOrdersCount(((Number) obj[2]).intValue());
            dto.setDeclinedOrdersCount(((Number) obj[3]).intValue());
            return dto;
        }).toList();
    }

    private List<MonthlyRevenue> getMonthlyRevenue() {
        List<Object[]> rawData = ordersRepository.findMonthlyRevenueRaw();
        return rawData.stream()
                .map(row -> new MonthlyRevenue(
                        (String) row[0], (BigDecimal) row[1]))
                .toList();
    }


    private RevenueStatistics getRevenueStatistics(){
        RevenueStatistics revenueStatistics = new RevenueStatistics();
        revenueStatistics.setTotalRevenue(ordersRepository.findTotalRevenue());
        revenueStatistics.setMonthlyRevenue(getMonthlyRevenue());
        return revenueStatistics;
    }


    private OrderStatistics getOrderStatistics(){
        OrderStatistics orderStatistics = new OrderStatistics();
        orderStatistics.setTotalOrderCount(ordersRepository.count());
        orderStatistics.setWeeklyOrderStats(getWeeklyOrderStats());
        return orderStatistics;
    }


    public DashboardStatistics getDashboardStats(Admin admin) {
        Admin theAdmin = verifyAdmin(admin);
        return new DashboardStatistics(
                getUserStatistics(),
                getRestaurantStatistics(),
                getFoodStatistics(),
                getOrderStatistics(),
                getRevenueStatistics()

        );
    }



}

