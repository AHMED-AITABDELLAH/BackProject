package com.savorybites.backend.api.dto.user;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class UserDetailsBody {

    private String firstName;
    private String lastName;
    private String phone;
    private String profileImg;
    private String email;
    private String oldPassword;
    private String newPassword;
}
