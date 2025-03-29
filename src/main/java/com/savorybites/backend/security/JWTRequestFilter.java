package com.savorybites.backend.security;

import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.repository.BlacklistedTokenRepository;
import com.savorybites.backend.repository.UserRepository;
import com.savorybites.backend.service.BlacklistedTokenService;
import com.savorybites.backend.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Component
public class JWTRequestFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserRepository userRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final BlacklistedTokenService blacklistedTokenService;

    public JWTRequestFilter(JWTService jwtService, UserRepository userRepository, BlacklistedTokenRepository blacklistedTokenRepository, BlacklistedTokenService blacklistedTokenService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.blacklistedTokenService = blacklistedTokenService;
    }

    @Override
    protected void doFilterInternal
            (
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain
            ) throws ServletException, IOException
    {
        String tokenHeader = request.getHeader("Authorization");
        if(tokenHeader != null && tokenHeader.startsWith("Bearer ")){
            String token = tokenHeader.substring(7);

            if(blacklistedTokenService.isTokenBlacklisted(token)){

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token is invalidated, please login again.");
                return;
            }
            UUID userID = jwtService.getUserID(token);
            Optional<LocalUser> userOptional= userRepository.findById(userID);
            if(userOptional.isPresent()){
                LocalUser user = userOptional.get();
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken
                        (
                                user,
                                null,
                                new ArrayList<>()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request,response);
    }
}
