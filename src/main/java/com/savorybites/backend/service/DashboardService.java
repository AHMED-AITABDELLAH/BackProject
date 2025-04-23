package com.savorybites.backend.service;

import com.savorybites.backend.api.dto.admin.*;
import com.savorybites.backend.api.dto.restaurant.RestaurantDashboardStatistics;
import com.savorybites.backend.model.Admin;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.model.Status;
import com.savorybites.backend.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrdersRepository ordersRepository;
    private final AdminRepository adminRepository;

    public DashboardService(FoodRepository foodRepository, UserRepository userRepository, RestaurantRepository restaurantRepository, OrdersRepository ordersRepository, AdminRepository adminRepository) {
        this.foodRepository = foodRepository;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.ordersRepository = ordersRepository;
        this.adminRepository = adminRepository;
    }


    private Admin verifyAdmin(Admin admin){
        return adminRepository.findById(admin.getId()).orElseThrow();
    }

    /// ////Dashboard Statistics for admin

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

    /// ////Dashboard Statistics for a restaurant

    private List<MonthlyRevenue> getRestaurantMonthlyRevenue(Restaurant restaurant) {
        List<Object[]> rawData = ordersRepository.findRestaurantMonthlyRevenueRaw(restaurant.getId());
        return rawData.stream()
                .map(row -> new MonthlyRevenue(
                        (String) row[0], (BigDecimal) row[1]))
                .toList();
    }

    private RevenueStatistics getRestaurantRevenueStatistics(Restaurant restaurant){
        RevenueStatistics revenueStatistics = new RevenueStatistics();
        revenueStatistics.setTotalRevenue(ordersRepository.findRestaurantTotalRevenue(restaurant.getId()));
        revenueStatistics.setMonthlyRevenue(getRestaurantMonthlyRevenue(restaurant));
        return revenueStatistics;
    }


    private List<WeeklyOrderStats> getRestaurantWeeklyOrderStats(Restaurant restaurant) {
        List<Object[]> rawData = ordersRepository.findRestaurantWeeklyOrderStats(restaurant.getId());

        return rawData.stream().map(obj -> {
            WeeklyOrderStats dto = new WeeklyOrderStats();
            dto.setWeekLabel((String) obj[0]);
            dto.setDeliveredOrdersCount(((Number) obj[1]).intValue());
            dto.setCancelledOrdersCount(((Number) obj[2]).intValue());
            dto.setDeclinedOrdersCount(((Number) obj[3]).intValue());
            return dto;
        }).toList();
    }

    private OrderStatistics getRestaurantOrderStatistics(Restaurant restaurant){
        OrderStatistics orderStatistics = new OrderStatistics();
        orderStatistics.setTotalOrderCount(ordersRepository.countByRestaurant(restaurant));
        orderStatistics.setWeeklyOrderStats(getRestaurantWeeklyOrderStats(restaurant));
        return orderStatistics;
    }


    private FoodStatistics getRestaurantFoodStatistics(Restaurant restaurant){
        FoodStatistics foodStatistics = new FoodStatistics();
        foodStatistics.setTotalFoodCount(foodRepository.countByRestaurant(restaurant));
        foodStatistics.setTotalFlaggedFood(foodRepository.countByRestaurantAndIsFlaggedTrue(restaurant));
        foodStatistics.setTotalUnflaggedFood();
        foodStatistics.setMostExpensiveFoodPrice(foodRepository.findRestaurantMostExpensiveFoodPrice(restaurant));
        foodStatistics.setLeastExpensiveFoodPrice(foodRepository.findRestaurantLeastExpensiveFoodPrice(restaurant));
        return foodStatistics;
    }

    public RestaurantDashboardStatistics getDashboardStats(Restaurant restaurant) {
        Restaurant theRestaurant = verifyRestaurant(restaurant);
        return new RestaurantDashboardStatistics(
                getRestaurantFoodStatistics(restaurant),
                getRestaurantOrderStatistics(restaurant),
                getRestaurantRevenueStatistics(restaurant)

        );
    }

    private Restaurant verifyRestaurant(Restaurant restaurant) {
        return restaurantRepository.findById(restaurant.getId()).orElseThrow();
    }


}
