package com.savorybites.backend.service;

import com.savorybites.backend.api.dto.food.FoodDetailsBody;
import com.savorybites.backend.api.dto.food.FoodRegistrationBody;
import com.savorybites.backend.exception.user.UserNotFoundException;
import com.savorybites.backend.model.Category;
import com.savorybites.backend.model.Food;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.repository.CategoryRepository;
import com.savorybites.backend.repository.FoodRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FoodService {

    public final FoodRepository foodRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final RestaurantService restaurantService;

    public FoodService(FoodRepository foodRepository, CategoryRepository categoryRepository, CategoryService categoryService, RestaurantService restaurantService) {
        this.foodRepository = foodRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.restaurantService = restaurantService;
    }


    /// ////////////////////////////////// Containers//////////////////////////

    private List<Food> getOnSaleItems(int discountBase){
        return foodRepository.findAllByDiscountGreaterThan(discountBase);
    }

    private List<Food> getListOfFood(){
        return foodRepository.findAllByIsAvailableTrue();
    }

    private List<Food> getListOfPopularFood(){
        return foodRepository.findByIsAvailableTrueOrderBySoldDesc();
    }


    private List<Food> getFoodFromRestaurant(long id){
        return foodRepository.findByRestaurantIdAndIsAvailableTrue(id);
    }

    private List<Food> getFoodByCategory(long categoryID){
        return foodRepository.findByCategories_Id(categoryID);
    }

    private List<Food> getFoodByCategories(List<Long> categoriesID){
        List<Category> categoryList =categoryService.getCategoriesFromIDs(categoriesID);
        return foodRepository.findDistinctByCategoriesInAndIsAvailableIsTrue(categoryList);
    }








/// ////////////////Manipulating Food user Exclusive////////////////////////////////////////////////7777

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
        List<Food> foodList=getListOfPopularFood();
        if (foodList.isEmpty())
            return null;
        Collections.shuffle(foodList);
        return foodList
                .stream()
                .limit(8)
                .collect(Collectors.toList());

    }

    //List all the food of a specified restaurant
    public List<Food> restaurantFood(long id){
        List<Food> foodList=getFoodFromRestaurant(id);
        if (foodList.isEmpty())
            return null;//TODO ADD EXCEPTION
        Collections.shuffle(foodList);
        return foodList;
    }

    //List all the food of a specific category
    public List<Food> foodByCategory(long categoryId){
        List<Food> foodList=getFoodByCategory(categoryId);
        if(foodList.isEmpty())
            return null;//TODO ADD EXCEPTION
        Collections.shuffle(foodList);
        return foodList;
    }

    //List all the food of a List of categories
    public List<Food> foodByCategories(List<Long> categoriesID){
        List<Food> foodList=getFoodByCategories(categoriesID);
        if(foodList.isEmpty())
            return null;//TODO ADD EXCEPTION
        Collections.shuffle(foodList);
        return foodList;
    }


    /// //////////////////////////////////////For Food Manipulation Restaurant Exclusive //////////////////////////////////////

    //Extracting Duplicated code
    private void assignValuesToFood(FoodRegistrationBody foodRegistrationBody, Food food) {

        food.setTitle(foodRegistrationBody.getTitle());
        food.setDescription(foodRegistrationBody.getDescription());
        food.setImage(foodRegistrationBody.getImage());
        food.setPrice(foodRegistrationBody.getPrice());
        food.setDiscount(foodRegistrationBody.getDiscount());
        food.setCategories(categoryRepository.findAllById(foodRegistrationBody.getCategoriesID()));
        food.setIsAvailable(foodRegistrationBody.getIsAvailable());
    }

    //Register a food
    public void addFood(Restaurant restaurant, FoodRegistrationBody foodRegistrationBody){
        Restaurant managedRestaurant=restaurantService.getManagedRestaurant(restaurant);
        Food food=new Food();
        assignValuesToFood(foodRegistrationBody, food);
        food.setRestaurant(managedRestaurant);
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
            assignValuesToFood(foodRegistrationBody, food);
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
            foodRepository.deleteByIdAndRestaurant(foodID,restaurant);
            return true;
        }
        return false;
    }

    //Disable food
    @Transactional
    public Boolean toggleFood(long foodID,Restaurant restaurant){
        Optional<Food> opFood=foodRepository.findFoodByIdAndRestaurant(foodID,restaurant);
        if(opFood.isPresent()){
            Food food= opFood.get();
            food.setIsAvailable(!food.getIsAvailable());
            foodRepository.save(food);
            return food.getIsAvailable();
        }
        throw new RuntimeException("Something went wrong");
    }


    //Get FoodItems of the LoggedIn Restaurant
    public List<FoodDetailsBody> getRestaurantFood(Restaurant restaurant){
        List<Food> foodList = foodRepository.findByRestaurant(restaurant);
        List<FoodDetailsBody> foodDetailsBodies = new ArrayList<>();
        for (Food food:foodList){
            foodDetailsBodies.add(new FoodDetailsBody(food));
        }
        return foodDetailsBodies;
    }


    //Get food by ID
    public FoodDetailsBody getFood(long foodID, Restaurant restaurant){
        Optional<Food> opFood = foodRepository.findFoodByIdAndRestaurant(foodID, restaurant);
        if (opFood.isPresent())
            return new FoodDetailsBody(opFood.get());
        else
            //TODO : Add Food not found exception
            throw new UserNotFoundException();
    }











    


}
