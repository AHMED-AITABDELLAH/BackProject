package com.savorybites.backend.exception;

public class UserNotApproved extends RuntimeException {


    public UserNotApproved(){
        super("The User is Not Approved by the Admins yet! you will receive an email for that!");
    }
}
