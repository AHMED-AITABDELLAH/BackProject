package com.savorybites.backend.api.user;

import com.savorybites.backend.api.dto.cart.CartItemDTO;
import com.savorybites.backend.api.dto.food.FoodDTO;
import com.savorybites.backend.model.CartItem;
import com.savorybites.backend.model.Category;
import com.savorybites.backend.model.Food;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.service.CartService;
import com.savorybites.backend.service.CategoryService;
import com.savorybites.backend.service.FoodService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/cart")
public class CartController {


    private final CartService cartService;
    private final FoodService foodService;
    public CartController(CartService cartService, FoodService foodService) {
        this.cartService = cartService;
        this.foodService = foodService;
    }


    @GetMapping("/")
    public List<CartItemDTO> cartContents(@AuthenticationPrincipal LocalUser user){
        List<CartItem> items = cartService.getCartContents(user);
        return items.stream().map(CartItemDTO::new).toList();
    }

    @PostMapping("/addItem")
    public ResponseEntity<Object> addToCart(@AuthenticationPrincipal LocalUser user,
                                            @RequestParam long foodID,
                                            @RequestParam(required = false) Integer qte){
        boolean success=cartService.addOrIncrementItemInCart(user, foodID,qte);
        return success
                ? ResponseEntity.ok("Item added successfully to your cart.")
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{itemID}/delete")
    public ResponseEntity<Object> deleteFromCart(@AuthenticationPrincipal LocalUser user,
                                                 @PathVariable long itemID){
        boolean success=cartService.deleteFromCart(user,itemID);
        return success
                ? ResponseEntity.ok("Item removed successfully from your cart.")
                : ResponseEntity.notFound().build();

    }

    @PutMapping("/{itemID}/increment")
    public ResponseEntity<Object> increment(@AuthenticationPrincipal LocalUser user,
                                            @PathVariable long itemID){
        boolean success = cartService.incrementItemQuantity(user,itemID);
        return success
                ? ResponseEntity.ok("Item incremented successfully .")
                : ResponseEntity.notFound().build();

    }

    @PutMapping("/{itemID}/decrement")
    public ResponseEntity<Object> decrement(@AuthenticationPrincipal LocalUser user,
                                            @PathVariable long itemID){
        boolean success = cartService.decrementItemQuantity(user,itemID);
        return success
                ? ResponseEntity.ok("Item decremented successfully .")
                : ResponseEntity.notFound().build();

    }

    @GetMapping("/youMayAlsoLike")
    public List<FoodDTO> getYouMayAlsoLikeFood(@AuthenticationPrincipal LocalUser user){

        List<Long> categoriesID = cartService.getCategoriesIDsInCart(user);

        List<Food> foodList=foodService.foodByCategoriesID(categoriesID);

        return foodList.stream().map(FoodDTO::new).toList();

    }

    @GetMapping("/total")
    public double total(@AuthenticationPrincipal LocalUser user){
        return cartService.getCartTotal(user);
    }

}
