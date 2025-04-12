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
        return restaurant.getAddress();
    }


    @PostMapping("/addAddress")
    public ResponseEntity<Object> addAddress(@AuthenticationPrincipal Restaurant restaurant,
                                             @RequestBody AddressRegistrationBody addressRegistrationBody)
    {
        boolean success=addressService.addAddress(restaurant,addressRegistrationBody,new Address());
        return success
                ?ResponseEntity.ok("Address added Successfully!")
                :ResponseEntity.internalServerError().body("Something went wrong.");
    }


    @PostMapping("/update")
    public ResponseEntity<Object> updateAddress(@AuthenticationPrincipal Restaurant restaurant,
                                                @RequestBody AddressRegistrationBody addressRegistrationBody)
    {
        long id = restaurant.getAddress().getId();
        boolean success=addressService.updateAddress(restaurant,addressRegistrationBody,id);
        return success
                ?ResponseEntity.ok("Address updated Successfully!")
                :ResponseEntity.internalServerError().body("Something went wrong.");
    }

}
