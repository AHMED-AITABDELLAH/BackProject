package com.savorybites.backend.exception.user;

public class EmailAlreadySentException extends RuntimeException {
    public EmailAlreadySentException(){
        super("The Email is already sent ! Try Again after a while.");
    }
}
