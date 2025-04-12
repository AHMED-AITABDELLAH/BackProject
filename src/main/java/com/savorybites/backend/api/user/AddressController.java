package com.savorybites.backend.api.user;

import com.savorybites.backend.api.dto.address.AddressRegistrationBody;
import com.savorybites.backend.model.Address;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.service.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userAddressController")
@RequestMapping("/user/address")
public class AddressController {


    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/hasAddress")
    public ResponseEntity<Object> hasAddress(@AuthenticationPrincipal LocalUser user){
        boolean success=addressService.hasAddress(user);
        return success
                ?ResponseEntity.ok("User has at least one address!")
                :ResponseEntity.noContent().build();
    }

    @GetMapping("/")
    public List<Address> getAddresses(@AuthenticationPrincipal LocalUser user){
        return addressService.getAddresses(user);
    }

    @PostMapping("/addAddress")
    public ResponseEntity<Object> addAddress(@AuthenticationPrincipal LocalUser user,
                                             @RequestBody AddressRegistrationBody addressRegistrationBody)
    {
        boolean success=addressService.addAddress(user,addressRegistrationBody);
        return success
                ?ResponseEntity.ok("Address added Successfully!")
                :ResponseEntity.internalServerError().body("Something went wrong.");
    }

    @GetMapping("/{id}")
    public Address getAddress(@AuthenticationPrincipal LocalUser user,
                                    @PathVariable long id){
        return addressService.getAddress(user,id);
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<Object> updateAddress(@AuthenticationPrincipal LocalUser user,
                                             @RequestBody AddressRegistrationBody addressRegistrationBody,
                                             @PathVariable long id)
    {
        boolean success=addressService.updateAddress(user,addressRegistrationBody,id);
        return success
                ?ResponseEntity.ok("Address updated Successfully!")
                :ResponseEntity.internalServerError().body("Something went wrong.");
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<Object> deleteAddress(@AuthenticationPrincipal LocalUser user,
                                             @PathVariable long id)
    {
        boolean success=addressService.deleteAddress(user,id);
        return success
                ?ResponseEntity.ok("Address deleted Successfully!")
                :ResponseEntity.internalServerError().body("Something went wrong.");
    }
}
