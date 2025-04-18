package com.savorybites.backend.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.BadCredentialsException;
@Getter
@Setter
public class BadAdminCredentialsException extends BadCredentialsException {

    private String clientIP;
    public BadAdminCredentialsException(String clientIP){
        super("Invalid Credentials!");
        this.clientIP = clientIP;
    }
}
