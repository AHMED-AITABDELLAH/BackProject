package com.savorybites.backend.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(){
        super("User Does Not Exist!");
    }
}
