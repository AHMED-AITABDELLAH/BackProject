package com.savorybites.backend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.savorybites.backend.model.LocalUser;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class JWTService {


    private static final String ID_KEY = "id";
    private static final String EMAIL_KEY = "email";

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
                .withClaim(EMAIL_KEY,user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + (expiryInSeconds*1000) ))
                .withIssuer(issuer)
                .sign(myAlgorithm);
    }


    public UUID getUserID(String token){
        return JWT.decode(token).getClaim(ID_KEY).as(UUID.class);
    }



}
