package com.savorybites.backend.api.dto;


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
    private String OldPassword;
    private String newPassword;
    private String contactEmail;
    private String paypalEmail;
    private String profileImg;
    private String profileBanner;

}
