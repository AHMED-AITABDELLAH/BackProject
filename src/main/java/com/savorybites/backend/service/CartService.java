package com.savorybites.backend.service;

import com.savorybites.backend.model.Cart;
import com.savorybites.backend.model.CartItem;
import com.savorybites.backend.model.Food;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.repository.CartItemRepository;
import com.savorybites.backend.repository.CartRepository;
import com.savorybites.backend.repository.FoodRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {


    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FoodRepository foodRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, FoodRepository foodRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.foodRepository = foodRepository;
    }

    //TODO ADD CUSTOM EXCEPTION
    public void createCart(LocalUser user) throws RuntimeException{
        Cart cart = new Cart();
        cart.setUser(user);
        cartRepository.save(cart);
    }

    //TODO ADD CUSTOM EXCEPTION
    //also used to refresh the cart.
    public List<CartItem> getCartContents(LocalUser user)throws RuntimeException{
        return cartItemRepository.findAllByCart(user.getCart());
    }


    private CartItem addToCart(LocalUser user,Food food){
            CartItem item=new CartItem();
            item.setFood(food);
            item.setCart(user.getCart());
            return item;
    }


    public boolean deleteFromCart(LocalUser user,long itemID){
        Optional<CartItem> opItem=cartItemRepository.findByItem_idAndCart(itemID,user.getCart());
        if(opItem.isPresent()){
            cartItemRepository.deleteByItem_id(itemID);
            return true;
        }
        return false;
    }

    public boolean decrementItemQuantity(LocalUser user, long itemID){
        Optional<CartItem> opItem=cartItemRepository.findByItem_idAndCart(itemID,user.getCart());
        if(opItem.isPresent()){
            CartItem item = opItem.get();
            item.decrement();
            if(item.getQuantity()<=0){
                cartItemRepository.deleteByItem_id(item.getItem_id());
                return true;
            }

            cartItemRepository.save(item);
            return true;
        }
        return false;
    }


    public boolean addOrIncrementItemInCart(LocalUser user,long foodID){
        Optional<Food> opFood=foodRepository.findById(foodID);
        if(opFood.isPresent()) {
            CartItem item;
            Food food = opFood.get();
            Optional<CartItem> opItem = cartItemRepository.findByCartAndFood(user.getCart(), food);
            if(opItem.isPresent()){
                item = opItem.get();
                item.increment();
            }else
                item=addToCart(user,food);
            cartItemRepository.save(item);
            return true;
        }
        return false;
    }



}
