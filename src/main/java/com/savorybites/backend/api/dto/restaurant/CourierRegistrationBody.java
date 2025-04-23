package com.savorybites.backend.api.dto.restaurant;

import com.savorybites.backend.model.Restaurant;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CourierRegistrationBody {

    private String fullName;

    private String email;

    private String phone;

    private String profileImg;

    private Boolean isAvailable;

}
