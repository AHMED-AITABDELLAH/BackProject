package com.savorybites.backend.api.dto.auth;

import com.savorybites.backend.api.dto.address.AddressRegistrationBody;
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
    private AddressRegistrationBody addressRegistrationBody;
    private String paypalEmail;
    private String contactEmail;
    private String password;
    private String address;

}
