package com.savorybites.backend.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@AllArgsConstructor
public class RegistrationBody {

    private String username;
    private String email;
    private String password;

}
