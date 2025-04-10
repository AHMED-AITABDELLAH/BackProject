package com.savorybites.backend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class RestaurantRegistrationBody {

    private String restaurantName;
    private String email;
    private String phone;
    private String paypalEmail;
    private String contactEmail;
    private String password;
    private String address;

}
