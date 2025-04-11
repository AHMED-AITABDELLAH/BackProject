package com.savorybites.backend.exception.user;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(){
        super("User Does Not Exist!");
    }
}
