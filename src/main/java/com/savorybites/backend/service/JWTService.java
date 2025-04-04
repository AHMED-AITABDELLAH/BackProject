package com.savorybites.backend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.savorybites.backend.model.LocalUser;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class JWTService {


    private static final String ID_KEY = "id";
    private static final String VERIFICATION_EMAIL_KEY = "verification email";
    private static final String PASSWORD_RESET_EMAIL_KEY = "password reset email";

    @Value("${jwt.algorithm-key}")
    private String algorithmKey;

    @Value("${jwt.issuer}")
    private String issuer;


    @Value("${jwt.expiryInSeconds}")
    private long expiryInSeconds;


    private Algorithm myAlgorithm;


    @PostConstruct
    public void PostConstruct(){
        myAlgorithm=Algorithm.HMAC256(algorithmKey);
    }


    public String generateToken(LocalUser user){
        return JWT.create()
                .withClaim(ID_KEY,user.getId().toString())
                .withExpiresAt(new Date(System.currentTimeMillis() + (expiryInSeconds*1000) ))
                .withIssuer(issuer)
                .sign(myAlgorithm);
    }

    public String generateVerificationJWT(LocalUser user){
        return JWT.create()
                .withClaim(VERIFICATION_EMAIL_KEY,user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + (expiryInSeconds*1000) ))
                .withIssuer(issuer)
                .sign(myAlgorithm);
    }
    public String generatePasswordResetJWT(LocalUser user){
        return JWT.create()
                .withClaim(PASSWORD_RESET_EMAIL_KEY,user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + (expiryInSeconds*1000) ))
                .withIssuer(issuer)
                .sign(myAlgorithm);
    }


    public UUID getUserID(String token){
        //this line of code verify the validity of the token and check if it was tempered with :)
        DecodedJWT jwt = JWT.require(myAlgorithm).withIssuer(issuer).build().verify(token);
        return jwt.getClaim(ID_KEY).as(UUID.class);
    }

    public String getPasswordResetEmail(String token){
        DecodedJWT jwt = JWT.require(myAlgorithm).withIssuer(issuer).build().verify(token);
        return jwt.getClaim(PASSWORD_RESET_EMAIL_KEY).asString();
    }



}
