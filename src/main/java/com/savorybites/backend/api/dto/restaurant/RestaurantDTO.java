package com.savorybites.backend.api.dto.restaurant;

import com.savorybites.backend.api.dto.address.AddressRegistrationBody;
import com.savorybites.backend.api.dto.address.AddressShortDTO;
import com.savorybites.backend.model.Restaurant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestaurantDTO {

    private Long id;
    private String title;
    private String phone;
    private AddressShortDTO addressShortDTO;
    private double shippingFees;
    private String contactEmail;
    private String profileImg;
    private String profileBanner;


    public RestaurantDTO(Restaurant restaurant){
        this.id = restaurant.getId();
        this.title = restaurant.getTitle();
        this.phone = restaurant.getPhone();
        this.addressShortDTO=new AddressShortDTO(restaurant.getAddress());
        this.contactEmail= restaurant.getContactEmail();
        this.shippingFees = restaurant.getShippingFees();
        this.profileImg = restaurant.getProfileImg();
        this.profileBanner = restaurant.getProfileBanner();

    }
}
