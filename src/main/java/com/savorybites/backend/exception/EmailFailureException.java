package com.savorybites.backend.exception;

public class EmailFailureException extends RuntimeException {
    public EmailFailureException(){
        super("Something Went Wrong!");
    }
}
