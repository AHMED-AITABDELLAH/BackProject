package com.savorybites.backend.security;

import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.repository.VerificationRepository.BlacklistedTokenRepository;
import com.savorybites.backend.repository.UserRepository;
import com.savorybites.backend.repository.VerificationRepository.RestaurantRepository;
import com.savorybites.backend.service.BlacklistedTokenService;
import com.savorybites.backend.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JWTRequestFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserRepository userRepository;
    private final BlacklistedTokenService blacklistedTokenService;
    private final RestaurantRepository restaurantRepository;

    public JWTRequestFilter(JWTService jwtService, UserRepository userRepository, BlacklistedTokenRepository blacklistedTokenRepository, BlacklistedTokenService blacklistedTokenService, RestaurantRepository restaurantRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.blacklistedTokenService = blacklistedTokenService;
        this.restaurantRepository = restaurantRepository;
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
        if(tokenHeader != null && tokenHeader.startsWith("Bearer "))
        {
            String token = tokenHeader.substring(7);

            if(blacklistedTokenService.isTokenBlacklisted(token)){

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token is invalidated, please login again.");
                return;
            }

            Long accountID = jwtService.getAccountID(token);
            String role = jwtService.getRoleFromToken(token);


            if("ROLE_USER".equals(role))
            {
                Optional<LocalUser> userOptional= userRepository.findLocalUserById(accountID);
                if(userOptional.isPresent())
                {
                    LocalUser user = userOptional.get();
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken
                            (
                                    user,
                                    null,
                                    List.of(new SimpleGrantedAuthority(role))
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            else if ("ROLE_RESTAURANT".equals(role))
            {
                Optional<Restaurant> restaurantOptional= restaurantRepository.findRestaurantById(accountID);
                if(restaurantOptional.isPresent())
                {
                    Restaurant restaurant = restaurantOptional.get();
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken
                            (
                                    restaurant,
                                    null,
                                    List.of(new SimpleGrantedAuthority(role))
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            }
        }
        filterChain.doFilter(request,response);
    }
}
