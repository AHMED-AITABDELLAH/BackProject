package com.savorybites.backend.api.restaurant;

import com.savorybites.backend.api.dto.restaurant.CourierRegistrationBody;
import com.savorybites.backend.model.Courier;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.service.CourierService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurant/couriers")
public class CourierHandlingController {

    private final CourierService courierService;

    public CourierHandlingController(CourierService courierService) {
        this.courierService = courierService;
    }

    @GetMapping("/")
    public List<Courier> getAllCouriers(@AuthenticationPrincipal Restaurant restaurant){
        return courierService.getAllCouriers(restaurant);
    }

    @GetMapping("/availableCouriers")
    public List<Courier> getAvailableCouriers(@AuthenticationPrincipal Restaurant restaurant){
        return courierService.getAvailableCouriers(restaurant);
    }


    @PostMapping("/addCourier")
    public ResponseEntity<?> addCourier(@AuthenticationPrincipal Restaurant restaurant,
                                        @RequestBody CourierRegistrationBody body){
        return courierService.addCourier(restaurant,body);
    }

    @DeleteMapping("/{courierID}/delete")
    public ResponseEntity<?> deleteCourier(@AuthenticationPrincipal Restaurant restaurant,
                                           @PathVariable long courierID){
        return courierService.deleteCourier(restaurant,courierID);
    }

    @PutMapping("/{courierID}/update")
    public ResponseEntity<?> updateCourier(@AuthenticationPrincipal Restaurant restaurant,
                                           @PathVariable long courierID,
                                           @RequestBody CourierRegistrationBody body){
        return courierService.updateCourier(restaurant,courierID,body);
    }

}
