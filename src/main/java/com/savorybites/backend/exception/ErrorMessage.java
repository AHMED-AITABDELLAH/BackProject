package com.savorybites.backend.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Setter
@Getter
public class ErrorMessage {
    // Getters and Setters
    private String message;
    private Date date;

    // Constructor
    public ErrorMessage(String message, Date date) {
        this.message = message;
        this.date = date;
    }
}
