package com.savorybites.backend.exception;



public class UserAlreadyExistsException extends RuntimeException{

    public UserAlreadyExistsException(){
        super("User Already Exists!");
    }
}
