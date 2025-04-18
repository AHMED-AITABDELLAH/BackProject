package com.savorybites.backend.api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserDTO {

    private String username;
    private String firstName;
    private String lastName;
    private String phone;
    private String profileImg;

}
