package com.savorybites.backend.api.restaurant;

import com.savorybites.backend.api.dto.address.AddressRegistrationBody;
import com.savorybites.backend.model.Address;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.service.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController("restaurantAddressController")
@RequestMapping("/restaurant/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/")
    public Address getAddress(@AuthenticationPrincipal Restaurant restaurant)
    {
        return addressService.getRestaurantAddress(restaurant);
    }


    @PostMapping("/addAddress")
    public ResponseEntity<Object> addAddress(@AuthenticationPrincipal Restaurant restaurant,
                                             @RequestBody AddressRegistrationBody addressRegistrationBody)
    {
        boolean success=addressService.addAddress(restaurant,addressRegistrationBody);
        return success
                ?ResponseEntity.ok("Address added Successfully!")
                :ResponseEntity.internalServerError().body("Something went wrong.");
    }


    @PostMapping("/update")
    public ResponseEntity<Object> updateAddress(@AuthenticationPrincipal Restaurant restaurant,
                                                @RequestBody AddressRegistrationBody addressRegistrationBody)
    {
        long id = addressService.getRestaurantAddress(restaurant).getId();
        boolean success=addressService.updateAddress(restaurant,addressRegistrationBody,id);
        return success
                ?ResponseEntity.ok("Address updated Successfully!")
                :ResponseEntity.internalServerError().body("Something went wrong.");
    }

    @PostMapping("/delete")
    public ResponseEntity<Object> deleteAddress(@AuthenticationPrincipal Restaurant restaurant)
    {
        long id = addressService.getRestaurantAddress(restaurant).getId();
        boolean success=addressService.deleteAddress(restaurant,id);
        return success
                ?ResponseEntity.ok("Address deleted Successfully!")
                :ResponseEntity.internalServerError().body("Something went wrong.");
    }

}
