package com.savorybites.backend.service;

import com.savorybites.backend.api.dto.Order.OrderDTO;
import com.savorybites.backend.api.dto.admin.*;
import com.savorybites.backend.api.dto.food.FoodDTO;
import com.savorybites.backend.api.dto.restaurant.FoodStockStatistics;
import com.savorybites.backend.api.dto.restaurant.RestaurantDTO;
import com.savorybites.backend.api.dto.restaurant.RestaurantDashboardStatistics;
import com.savorybites.backend.api.dto.user.UserDTO;
import com.savorybites.backend.model.Admin;
import com.savorybites.backend.model.OrderStatus;
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
        foodStatistics.setTopSellingFoods(foodRepository.findTop3BestSellingFoodsGlobally().stream().map(FoodDTO::new).toList());
        return foodStatistics;
    }

    private UserStatistics getUserStatistics(){
        UserStatistics userStatistics=new UserStatistics();
        userStatistics.setTotalUserCount(userRepository.count());
        userStatistics.setBannedUserCount(userRepository.countByStatus(Status.BANNED));
        userStatistics.setVerifiedUserCount(userRepository.countByStatus(Status.VERIFIED));
        userStatistics.setTopSpendingUsers(ordersRepository.findTop3SpendingUsers().stream().map(UserDTO::new).toList());
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
        restaurantStatistics.setMostPopularRestaurants(foodRepository
                .findTop3RestaurantsByFoodSold()
                .stream().map(RestaurantDTO::new).toList());
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
        orderStatistics.setUncompletedOrderCount(ordersRepository.countAllByStatus(OrderStatus.UNCOMPLETED));
        orderStatistics.setCompletedOrderCount(ordersRepository.countAllByStatus(OrderStatus.COMPLETED));
        orderStatistics.setAcceptedOrderCount(ordersRepository.countAllByStatus(OrderStatus.ACCEPTED));
        orderStatistics.setDeliveredOrderCount(ordersRepository.countAllByStatus(OrderStatus.DELIVERED));
        orderStatistics.setCancelledOrderCount(ordersRepository.countAllByStatus(OrderStatus.CANCELLED));
        orderStatistics.setPaymentFailedOrderCount(ordersRepository.countAllByStatus(OrderStatus.PAYMENT_FAILED));
        orderStatistics.setRejectedOrderCount(ordersRepository.countAllByStatus(OrderStatus.REJECTED));
        orderStatistics.setWeeklyOrderStats(getWeeklyOrderStats());
        orderStatistics.setTopBigOrders(ordersRepository
                .findTop3ExpensiveOrders()
                .stream().map(OrderDTO::new).toList());
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
        orderStatistics.setUncompletedOrderCount(ordersRepository.countAllByStatusAndRestaurant(OrderStatus.UNCOMPLETED,restaurant));
        orderStatistics.setCompletedOrderCount(ordersRepository.countAllByStatusAndRestaurant(OrderStatus.COMPLETED,restaurant));
        orderStatistics.setAcceptedOrderCount(ordersRepository.countAllByStatusAndRestaurant(OrderStatus.ACCEPTED,restaurant));
        orderStatistics.setDeliveredOrderCount(ordersRepository.countAllByStatusAndRestaurant(OrderStatus.DELIVERED,restaurant));
        orderStatistics.setCancelledOrderCount(ordersRepository.countAllByStatusAndRestaurant(OrderStatus.CANCELLED,restaurant));
        orderStatistics.setPaymentFailedOrderCount(ordersRepository.countAllByStatusAndRestaurant(OrderStatus.PAYMENT_FAILED,restaurant));
        orderStatistics.setRejectedOrderCount(ordersRepository.countAllByStatusAndRestaurant(OrderStatus.REJECTED,restaurant));
        orderStatistics.setTotalOrderCount(ordersRepository.countByRestaurant(restaurant));
        orderStatistics.setWeeklyOrderStats(getRestaurantWeeklyOrderStats(restaurant));
        orderStatistics.setTopBigOrders(ordersRepository
                .findTop3ExpensiveOrdersByRestaurant(restaurant.getId())
                .stream().map(OrderDTO::new).toList());
        return orderStatistics;
    }

    private FoodStatistics getRestaurantFoodStatistics(Restaurant restaurant){
        FoodStatistics foodStatistics = new FoodStatistics();
        foodStatistics.setTotalFoodCount(foodRepository.countByRestaurant(restaurant));
        foodStatistics.setTotalFlaggedFood(foodRepository.countByRestaurantAndIsFlaggedTrue(restaurant));
        foodStatistics.setTotalUnflaggedFood();
        foodStatistics.setMostExpensiveFoodPrice(foodRepository.findRestaurantMostExpensiveFoodPrice(restaurant));
        foodStatistics.setLeastExpensiveFoodPrice(foodRepository.findRestaurantLeastExpensiveFoodPrice(restaurant));
        foodStatistics.setTopSellingFoods(foodRepository.
                findTop3BestSellingFoodsByRestaurant(restaurant.getId()).
                stream().map(FoodDTO::new).toList());
        return foodStatistics;
    }

    private FoodStockStatistics getRestaurantFoodStockStatistics(Restaurant restaurant){
        FoodStockStatistics foodStockStatistics = new FoodStockStatistics();
        foodStockStatistics.setTotalFoodCount(foodRepository.countByRestaurant(restaurant));
        foodStockStatistics.setTotalAvailableFood(foodRepository.countByIsAvailableTrueAndRestaurant(restaurant));
        foodStockStatistics.setTotalUnavailableFood(foodRepository.countByIsAvailableFalseAndRestaurant(restaurant));
        return foodStockStatistics;
    }

    public RestaurantDashboardStatistics getDashboardStats(Restaurant restaurant) {
        Restaurant theRestaurant = verifyRestaurant(restaurant);
        return new RestaurantDashboardStatistics(
                getRestaurantFoodStockStatistics(restaurant),
                getRestaurantFoodStatistics(restaurant),
                getRestaurantOrderStatistics(restaurant),
                getRestaurantRevenueStatistics(restaurant)
        );
    }

    private Restaurant verifyRestaurant(Restaurant restaurant) {
        return restaurantRepository.findById(restaurant.getId()).orElseThrow();
    }


}
