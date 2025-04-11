package com.savorybites.backend.exception.user;

public class PasswordIncorrectException extends RuntimeException {
    public PasswordIncorrectException() {
        super("The Password Is Incorrect!");
    }
}
