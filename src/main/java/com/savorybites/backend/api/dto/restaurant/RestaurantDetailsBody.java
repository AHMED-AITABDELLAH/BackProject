package com.savorybites.backend.api.dto.restaurant;


import com.savorybites.backend.api.dto.address.AddressRegistrationBody;
import com.savorybites.backend.model.Address;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class RestaurantDetailsBody {

    private String title;
    private String phone;
    private String email;
    private AddressRegistrationBody addressRegistrationBody;
    private String OldPassword;
    private String newPassword;
    private String contactEmail;
    private String paypalEmail;
    private String profileImg;
    private String profileBanner;

}
