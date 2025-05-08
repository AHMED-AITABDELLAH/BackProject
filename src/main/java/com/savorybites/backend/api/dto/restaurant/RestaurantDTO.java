package com.savorybites.backend.api.dto.restaurant;

import com.savorybites.backend.api.dto.address.AddressShortDTO;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.model.Status;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestaurantDTO {

    private Long id;
    private String title;
    private String phone;
    private AddressShortDTO addressShortDTO;
    private String email;
    private double shippingFees;
    private String contactEmail;
    private String profileImg;
    private String profileBanner;
    private Status status;


    public RestaurantDTO(Restaurant restaurant){
        this.id = restaurant.getId();
        this.title = restaurant.getTitle();
        this.phone = restaurant.getPhone();
        this.email= restaurant.getEmail();
        this.addressShortDTO=new AddressShortDTO(restaurant.getAddress());
        this.contactEmail= restaurant.getContactEmail();
        this.shippingFees = restaurant.getShippingFees();
        this.profileImg = restaurant.getProfileImg();
        this.status = restaurant.getStatus();
        this.profileBanner = restaurant.getProfileBanner();

    }
}
