package com.savorybites.backend.service;

import com.savorybites.backend.api.model.FoodRegistrationBody;
import com.savorybites.backend.model.Food;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.repository.FoodRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FoodService {

    public final FoodRepository foodRepository;

    public FoodService(FoodRepository foodRepository) {
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
    public ResponseEntity<Object> updateFood(Restaurant restaurant,
                                     FoodRegistrationBody foodRegistrationBody,
                                     long foodID){
        Optional<Food> opFood=foodRepository.findFoodByIdAndRestaurant(foodID,restaurant);
        if(opFood.isPresent()){
            Food food= opFood.get();

            food.setTitle(foodRegistrationBody.getTitle());
            food.setDescription(foodRegistrationBody.getDescription());
            food.setImage(foodRegistrationBody.getImage());
            food.setPrice(foodRegistrationBody.getPrice());
            food.setDiscount(foodRegistrationBody.getDiscount());
            food.setIsAvailable(foodRegistrationBody.getIsAvailable());

            foodRepository.save(food);
            return ResponseEntity.ok("Your food item is successfully updated");
        }
        //TODO :item not found Exception
        return ResponseEntity.status(HttpStatus.CONFLICT).build();

    }

    //Delete a food
    @Transactional
    public boolean deleteFood(long foodID,Restaurant restaurant){
        Optional<Food> opFood=foodRepository.findFoodByIdAndRestaurant(foodID,restaurant);
        if(opFood.isPresent()){
            foodRepository.deleteFoodById(foodID);
            return true;
        }
        return false;
    }

    //Disable food
    @Transactional
    public Boolean disableFood(long foodID,Restaurant restaurant){
        Optional<Food> opFood=foodRepository.findFoodByIdAndRestaurant(foodID,restaurant);
        if(opFood.isPresent()){
            Food food= opFood.get();
            food.setIsAvailable(false);
            foodRepository.save(food);
            return true;
        }
        return false;
    }







    


}
