package com.savorybites.backend.exception;

public class UserNotVerified extends RuntimeException {
    public UserNotVerified() {
        super("Your Email Is Not Verified!");
    }
}
