package com.savorybites.backend.exception.user;

public class EmailFailureException extends RuntimeException {
    public EmailFailureException(){
        super("Failed to send email!");
    }
}
