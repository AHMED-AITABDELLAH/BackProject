package com.savorybites.backend.api.dto.user;

import com.savorybites.backend.model.LocalUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserDTO {

    private long id;
    private String username;
    private String firstName;
    private String lastName;
    private String phone;
    private String profileImg;



    public UserDTO(LocalUser user){
        this.id = user.getId();
        this.username = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.phone = user.getPhone();
        this.profileImg = user.getProfileImg();
    }

}
