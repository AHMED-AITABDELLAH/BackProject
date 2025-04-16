package com.savorybites.backend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.savorybites.backend.model.Admin;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.Restaurant;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class JWTService {

    private static final String ID_KEY = "id";
    private static final String ROLE = "role";
    private static final String VERIFICATION_EMAIL_KEY = "verification_email";
    private static final String PASSWORD_RESET_EMAIL_KEY = "password_reset_email";

    @Value("${jwt.algorithm-key}")
    private String algorithmKey;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expiryInSeconds}")
    private long expiryInSeconds;

    private Algorithm myAlgorithm;

    @PostConstruct
    public void PostConstruct(){
        myAlgorithm = Algorithm.HMAC256(algorithmKey);
    }



    // Generate a token for LocalUser (Client)
    public String generateToken(LocalUser user) {
        return JWT.create()
                .withClaim(ID_KEY, user.getId().toString())
                .withClaim(ROLE,user.getRole().toString())
                .withExpiresAt(new Date(System.currentTimeMillis() + (expiryInSeconds * 1000)))
                .withIssuer(issuer)
                .sign(myAlgorithm);
    }
    // Generate a token for Restaurant
    public String generateToken(Restaurant restaurant) {
        return JWT.create()
                .withClaim(ID_KEY, restaurant.getId().toString())
                .withClaim(ROLE,restaurant.getRole().toString())
                .withExpiresAt(new Date(System.currentTimeMillis() + (expiryInSeconds * 1000)))
                .withIssuer(issuer)
                .sign(myAlgorithm);
    }
    // Generate a token for Restaurant
    public String generateToken(Admin admin) {
        return JWT.create()
                .withClaim(ID_KEY, admin.getId().toString())
                .withClaim(ROLE,admin.getRole().toString())
                .withExpiresAt(new Date(System.currentTimeMillis() + (expiryInSeconds * 1000)))
                .withIssuer(issuer)
                .sign(myAlgorithm);
    }


    // Generate a verification token for LocalUser (Client)
    public String generateVerificationJWT(LocalUser user) {
        return JWT.create()
                .withClaim(VERIFICATION_EMAIL_KEY, user.getEmail())
                .withClaim(ROLE,user.getRole().toString())
                .withExpiresAt(new Date(System.currentTimeMillis() + (expiryInSeconds * 1000)))
                .withIssuer(issuer)
                .sign(myAlgorithm);
    }

    // Generate a verification token for Restaurant
    public String generateVerificationJWT(Restaurant restaurant) {
        return JWT.create()
                .withClaim(ID_KEY, restaurant.getId().toString())
                .withClaim(ROLE,restaurant.getRole().toString())
                .withExpiresAt(new Date(System.currentTimeMillis() + (expiryInSeconds * 1000)))
                .withIssuer(issuer)
                .sign(myAlgorithm);
    }

    // Generate a password reset token for LocalUser (Client)
    public String generatePasswordResetJWT(LocalUser user) {
        return JWT.create()
                .withClaim(PASSWORD_RESET_EMAIL_KEY, user.getEmail())
                .withClaim(ROLE,user.getRole().toString())
                .withExpiresAt(new Date(System.currentTimeMillis() + (expiryInSeconds * 1000)))
                .withIssuer(issuer)
                .sign(myAlgorithm);
    }

    // Generate a password reset token for Restaurant
    public String generatePasswordResetJWT(Restaurant restaurant) {
        return JWT.create()
                .withClaim(PASSWORD_RESET_EMAIL_KEY, restaurant.getEmail())
                .withClaim(ROLE,restaurant.getRole().toString())
                .withExpiresAt(new Date(System.currentTimeMillis() + (expiryInSeconds * 1000)))
                .withIssuer(issuer)
                .sign(myAlgorithm);
    }

    // Get the user ID from the token works both for Users and Restaurants
    public Long getAccountID(String token) {
        DecodedJWT jwt = JWT.require(myAlgorithm).withIssuer(issuer).build().verify(token);
        return jwt.getClaim(ID_KEY).as(Long.class);
    }


    // Get the password reset email from the token (for users)
    public String getPasswordResetEmail(String token) {
        DecodedJWT jwt = JWT.require(myAlgorithm)
                .withIssuer(issuer)
                .build()
                .verify(token);
        return jwt.getClaim(PASSWORD_RESET_EMAIL_KEY).asString();
    }

    // Extract the verification email from the token (for users)
    public String getVerificationEmail(String token) {
        DecodedJWT jwt = JWT.require(myAlgorithm)
                .withIssuer(issuer)
                .build()
                .verify(token);
        return jwt.getClaim(VERIFICATION_EMAIL_KEY).asString();
    }

    //Extract the Role from the token
    public String getRoleFromToken(String token) {
        DecodedJWT jwt = JWT.require(myAlgorithm)
                .withIssuer(issuer)
                .build()
                .verify(token);
        return jwt.getClaim(ROLE).asString();
    }


}
