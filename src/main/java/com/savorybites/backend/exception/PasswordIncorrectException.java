package com.savorybites.backend.exception;

public class PasswordIncorrectException extends RuntimeException {
    public PasswordIncorrectException() {
        super("The Password Is Incorrect!");
    }
}
