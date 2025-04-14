package com.savorybites.backend.service;

import com.savorybites.backend.model.Cart;
import com.savorybites.backend.model.CartItem;
import com.savorybites.backend.model.Food;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.repository.CartItemRepository;
import com.savorybites.backend.repository.CartRepository;
import com.savorybites.backend.repository.FoodRepository;
import jakarta.transaction.Transactional;
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


    private CartItem addToCart(Cart cart,Food food){
            CartItem item=new CartItem();
            item.setFood(food);
            item.setCart(cart);
            return item;
    }

    @Transactional
    public boolean deleteFromCart(LocalUser user,long itemID){
        Optional<CartItem> opItem=cartItemRepository.findByItemIDAndCart(itemID,user.getCart());
        if(opItem.isPresent()){
            cartItemRepository.delete(opItem.get());
            return true;
        }
        return false;
    }
    @Transactional
    public boolean decrementItemQuantity(LocalUser user, long itemID){
        Cart managedCart = getManagedCart(user);
        if(managedCart==null)
            return false;
        Optional<CartItem> opItem=cartItemRepository.findByItemIDAndCart(itemID,managedCart);
        if(opItem.isPresent()){
            CartItem item = opItem.get();
            item.decrement();
            if(item.getQuantity()<=0){
                cartItemRepository.delete(item);
                return true;
            }

            cartItemRepository.save(item);
            return true;
        }
        return false;
    }
    @Transactional
    public boolean incrementItemQuantity(LocalUser user, long itemID) {
        Cart managedCart = getManagedCart(user);
        if(managedCart==null)
            return false;

        Optional<CartItem> opItem=cartItemRepository.findByItemIDAndCart(itemID,managedCart);
        if(opItem.isPresent()) {
            CartItem item = opItem.get();
            item.increment();
            return true;
        }
        return false;
    }

    private Cart getManagedCart(LocalUser user){
        return cartRepository.findByUser(user).orElse(null);
    }

    @Transactional
    public boolean addOrIncrementItemInCart(LocalUser user,long foodID){
        Optional<Food> opFood=foodRepository.findById(foodID);
        Cart managedCart = getManagedCart(user);
        if(managedCart==null)
            return false;
        if(opFood.isPresent()) {
            CartItem item;
            Food food = opFood.get();
            Optional<CartItem> opItem = cartItemRepository.findByCartAndFood(managedCart, food);
            if(opItem.isPresent()){
                item = opItem.get();
                item.increment();
            }else
                item=addToCart(managedCart,food);
            cartItemRepository.save(item);
            return true;
        }
        return false;
    }


    public double getCartTotal(LocalUser user){
        Optional<Cart> optionalUserCart= cartRepository.findByUser(user);

        if(optionalUserCart.isEmpty())
            throw  new RuntimeException();//TODO CREATE EXCEPTION

        Cart userCart=optionalUserCart.get();
        List<CartItem> userItems =cartItemRepository.findAllByCart(userCart);
        double sum=0.0;
        if(userItems.isEmpty())
            return sum;
        for(CartItem item:userItems){
            sum+=item.getTotalPrice();
        }
        return sum;
    }





}































/*public OrderDetails Order(LocalUser user){
        Optional<Cart> optionalUserCart= cartRepository.findByUser(user);

        if(optionalUserCart.isEmpty())
            throw  new RuntimeException();//TODO CREATE EXCEPTION

        Cart userCart=optionalUserCart.get();
        List<CartItem> userItems =cartItemRepository.findAllByCart(userCart);

        if(userItems.isEmpty())
            throw new RuntimeException();//TODO CREATE EXCEPTION

        OrderDetails orderDetails =new OrderDetails();

        for(CartItem item:userItems){
            ItemDetails itemDetails = new ItemDetails(
                    item.getFood().getTitle(),
                    item.getQuantity(),
                    item.getTotalPrice());

            orderDetails.add(itemDetails);
            orderDetails.setShipping(item.getFood().getRestaurant().getShippingFees());
            orderDetails.setSubTotal();
            orderDetails.setTotal();
        }

        return null;
    }*/
