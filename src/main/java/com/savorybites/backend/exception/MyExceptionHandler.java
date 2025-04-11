package com.savorybites.backend.exception;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.savorybites.backend.exception.user.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

@ControllerAdvice
public class MyExceptionHandler extends ResponseEntityExceptionHandler {


    //HANDLING ANY ERROR
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<Object> handleAnyException
            (Exception exception)
    {
        String errorMsgDesc=exception.getLocalizedMessage();
        if(errorMsgDesc==null) errorMsgDesc=exception.toString();
        ErrorMessage errorMessage = new ErrorMessage(errorMsgDesc,new Date());


        return new ResponseEntity<>(
                errorMessage,new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR
        );
    }


    //HANDLING UserAlreadyExistsException
    @ExceptionHandler(value = {UserAlreadyExistsException.class})
    public ResponseEntity<Object> handleUserAlreadyExistsException
    (UserAlreadyExistsException exception)
    {
        String errorMsgDesc=exception.getLocalizedMessage();
        if(errorMsgDesc==null) errorMsgDesc=exception.toString();
        ErrorMessage errorMessage = new ErrorMessage(errorMsgDesc,new Date());


        return new ResponseEntity<>(
                errorMessage,new HttpHeaders(), HttpStatus.CONFLICT
        );
    }

    //HANDLING UserNotFoundException
    @ExceptionHandler(value = {UserNotFoundException.class})
    public ResponseEntity<Object> handleUserNotFoundException
    (UserNotFoundException exception)
    {
        String errorMsgDesc=exception.getLocalizedMessage();
        if(errorMsgDesc==null) errorMsgDesc=exception.toString();
        ErrorMessage errorMessage = new ErrorMessage(errorMsgDesc,new Date());


        return new ResponseEntity<>(
                errorMessage,new HttpHeaders(), HttpStatus.BAD_REQUEST
        );
    }

    //HANDLING PasswordIncorrectException
    @ExceptionHandler(value = {PasswordIncorrectException.class})
    public ResponseEntity<Object> handlePasswordIncorrectException
    (PasswordIncorrectException exception)
    {
        String errorMsgDesc=exception.getLocalizedMessage();
        if(errorMsgDesc==null) errorMsgDesc=exception.toString();
        ErrorMessage errorMessage = new ErrorMessage(errorMsgDesc,new Date());


        return new ResponseEntity<>(
                errorMessage,new HttpHeaders(), HttpStatus.BAD_REQUEST
        );
    }

    //HANDLING InvalidTokenException
    @ExceptionHandler(value = {JWTDecodeException.class})
    public ResponseEntity<Object> JWTDecodeException
    (JWTDecodeException exception)
    {
        String errorMsgDesc=exception.getLocalizedMessage();
        if(errorMsgDesc==null) errorMsgDesc=exception.toString();
        ErrorMessage errorMessage = new ErrorMessage(errorMsgDesc,new Date());


        return new ResponseEntity<>(
                errorMessage,new HttpHeaders(), HttpStatus.BAD_REQUEST
        );
    }

    //HANDLING EmailFailureException
    @ExceptionHandler(value = {EmailFailureException.class})
    public ResponseEntity<Object> EmailFailureException
    (EmailFailureException exception)
    {
        String errorMsgDesc=exception.getLocalizedMessage();
        if(errorMsgDesc==null) errorMsgDesc=exception.toString();
        ErrorMessage errorMessage = new ErrorMessage(errorMsgDesc,new Date());


        return new ResponseEntity<>(
                errorMessage,new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    //HANDLING UserNotVerified
    @ExceptionHandler(value = {UserNotVerified.class})
    public ResponseEntity<Object> UserNotVerified
    (UserNotVerified exception)
    {
        String errorMsgDesc=exception.getLocalizedMessage();
        if(errorMsgDesc==null) errorMsgDesc=exception.toString();
        ErrorMessage errorMessage = new ErrorMessage(errorMsgDesc,new Date());


        return new ResponseEntity<>(
                errorMessage,new HttpHeaders(), HttpStatus.UNAUTHORIZED
        );
    }


    //HANDLING EmailAlreadySentException
    @ExceptionHandler(value = {EmailAlreadySentException.class})
    public ResponseEntity<Object> EmailAlreadySentException
            (EmailAlreadySentException exception)
    {
        String errorMsgDesc=exception.getLocalizedMessage();
        if(errorMsgDesc==null) errorMsgDesc=exception.toString();
        ErrorMessage errorMessage = new ErrorMessage(errorMsgDesc,new Date());


        return new ResponseEntity<>(
                errorMessage,new HttpHeaders(), HttpStatus.CONFLICT
        );
    }



    //HANDLING UserNotApproved
    @ExceptionHandler(value = {UserNotApproved.class})
    public ResponseEntity<Object> UserNotApproved
    (UserNotApproved exception)
    {
        String errorMsgDesc=exception.getLocalizedMessage();
        if(errorMsgDesc==null) errorMsgDesc=exception.toString();
        ErrorMessage errorMessage = new ErrorMessage(errorMsgDesc,new Date());


        return new ResponseEntity<>(
                errorMessage,new HttpHeaders(), HttpStatus.UNAUTHORIZED
        );
    }
}