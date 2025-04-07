package com.savorybites.backend.exception;

public class UserNotVerified extends RuntimeException {
    public UserNotVerified() {
        super("Your Account Is Not Verified!please check your email inbox.");
    }
}
