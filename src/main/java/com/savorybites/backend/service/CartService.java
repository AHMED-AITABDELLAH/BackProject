package com.savorybites.backend.service;

import com.savorybites.backend.model.*;
import com.savorybites.backend.repository.CartItemRepository;
import com.savorybites.backend.repository.CartRepository;
import com.savorybites.backend.repository.FoodRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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


    private CartItem addToCart(Cart cart,Food food,Integer qte){
            CartItem item=new CartItem();
            item.setFood(food);
            item.setCart(cart);
            item.setQuantity(qte);
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

    public Cart getManagedCart(LocalUser user){
        return cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user"));
    }

    @Transactional
    public boolean addOrIncrementItemInCart(LocalUser user,long foodID,Integer qte){
        if(qte==null)
            qte=1;
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
                item.increment(qte);
            }else
                item=addToCart(managedCart,food,qte);
            cartItemRepository.save(item);
            return true;
        }
        return false;
    }


    public double getCartTotal(LocalUser user){
        Optional<Cart> optionalUserCart= cartRepository.findByUser(user);

        if(optionalUserCart.isEmpty())
            return 0.0;

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

    public double getShippingTotal(LocalUser user){
        List<CartItem> cartItems = getCartContents(user);
        return cartItems.stream()
                .map(item -> item.getFood().getRestaurant())
                .distinct()
                .mapToDouble(Restaurant::getShippingFees)
                .sum();
    }


    public List<Long> getCategoriesIDsInCart(LocalUser user){

        List<CartItem> cartItems = getCartContents(user);
        List<Long> categoriesIDList = new ArrayList<>();
        for(CartItem item : cartItems){
            List<Category> itemCategories = item.getFood().getCategories();
            for(Category category : itemCategories)
                categoriesIDList.add(category.getId());

        }
        return categoriesIDList;
    }




}































/*public OrderDetailsDescription Orders(LocalUser user){
        Optional<Cart> optionalUserCart= cartRepository.findByUser(user);

        if(optionalUserCart.isEmpty())
            throw  new RuntimeException();//TODO CREATE EXCEPTION

        Cart userCart=optionalUserCart.get();
        List<CartItem> userItems =cartItemRepository.findAllByCart(userCart);

        if(userItems.isEmpty())
            throw new RuntimeException();//TODO CREATE EXCEPTION

        OrderDetailsDescription orderDetails =new OrderDetailsDescription();

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
