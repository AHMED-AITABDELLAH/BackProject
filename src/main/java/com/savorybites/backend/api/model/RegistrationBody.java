package com.savorybites.backend.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@AllArgsConstructor
public class RegistrationBody {

    private String firstName;
    private String lastName;
    private String email;
    private String password;

}
