package com.savorybites.backend.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import lombok.Setter;

@Setter
@Getter


public class LoginBody {//This is a DTO


    @NotNull
    @NotBlank
    private String email;


    @NotNull
    @NotBlank
    private String password;
}
