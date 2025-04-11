package com.savorybites.backend.exception.user;



public class UserAlreadyExistsException extends RuntimeException{

    public UserAlreadyExistsException(){
        super("User Already Exists!");
    }
}
