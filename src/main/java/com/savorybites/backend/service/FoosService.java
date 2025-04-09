package com.savorybites.backend.service;

import com.savorybites.backend.api.model.FoodRegistrationBody;
import com.savorybites.backend.model.Food;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.repository.FoodRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FoosService {

    public final FoodRepository foodRepository;

    public FoosService(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    private List<Food> getOnSaleItems(int discountBase){
        return foodRepository.findAllByDiscountGreaterThan(discountBase);
    }
    private List<Food> getListOfFood(){
        return foodRepository.findAllByIsAvailableTrue();
    }
    private List<Food> getFoodFromRestaurant(long id){
        return foodRepository.findByRestaurantIdAndIsAvailableTrue(id);
    }

    //We get 4 randomized items from the Discounted food items
    public List<Food> OnSaleSection(int discountBase){
        List<Food> foodList=getOnSaleItems(discountBase);
        if (foodList.isEmpty())
            return null;
        Collections.shuffle(foodList);
        return foodList
                .stream()
                .limit(4)
                .collect(Collectors.toList());
    }

    //Get ( popular food items )
    public List<Food> PopularFoodItems(){
        List<Food> foodList=getListOfFood();
        if (foodList.isEmpty())
            return null;
        Collections.shuffle(foodList);
        return foodList
                .stream()
                .limit(8)
                .collect(Collectors.toList());

    }

    //List all the food of a specified restaurant
    public List<Food> RestaurantFood(long id){
        List<Food> foodList=getFoodFromRestaurant(id);
        if (foodList.isEmpty())
            return null;
        Collections.shuffle(foodList);
        return foodList;
    }

    //List all the food of a specific category
    //TODO: ADD CATEGORIES



    //Register a food
    public void addFood(Restaurant restaurant, FoodRegistrationBody foodRegistrationBody){
        Food food=new Food();
        food.setTitle(foodRegistrationBody.getTitle());
        food.setDescription(foodRegistrationBody.getDescription());
        food.setImage(foodRegistrationBody.getImage());
        food.setPrice(foodRegistrationBody.getPrice());
        food.setDiscount(foodRegistrationBody.getDiscount());
        food.setIsAvailable(foodRegistrationBody.getIsAvailable());
        food.setRestaurant(restaurant);
        foodRepository.save(food);

    }

    //Update a food
    @Transactional
    public void updateFood(Restaurant restaurant, FoodRegistrationBody foodRegistrationBody,long foodID){
        Optional<Food> opFood=foodRepository.findFoodById(foodID);
        if(opFood.isPresent()){
            Food food= opFood.get();

            food.setTitle(foodRegistrationBody.getTitle());
            food.setDescription(foodRegistrationBody.getDescription());
            food.setImage(foodRegistrationBody.getImage());
            food.setPrice(foodRegistrationBody.getPrice());
            food.setDiscount(foodRegistrationBody.getDiscount());
            food.setIsAvailable(foodRegistrationBody.getIsAvailable());
            food.setRestaurant(restaurant);
            foodRepository.save(food);
        }


    }

    //Delete a food
    @Transactional
    public void deleteFood(long foodID){
        Optional<Food> opFood=foodRepository.findFoodById(foodID);
        if(opFood.isPresent()){
            Food food= opFood.get();
            foodRepository.deleteFoodById(foodID);
        }
    }






    


}
