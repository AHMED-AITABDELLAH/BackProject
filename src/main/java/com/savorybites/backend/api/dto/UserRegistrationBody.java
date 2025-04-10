package com.savorybites.backend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@AllArgsConstructor
public class UserRegistrationBody {

    private String username;
    private String email;
    private String password;

}
