package com.savorybites.backend.security;
import com.savorybites.backend.repository.AdminRepository;
import com.savorybites.backend.repository.UserRepository;
import com.savorybites.backend.repository.RestaurantRepository;
import com.savorybites.backend.service.BlacklistedTokenService;
import com.savorybites.backend.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class JWTRequestFilter extends OncePerRequestFilter implements ChannelInterceptor {

    private final JWTService jwtService;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final BlacklistedTokenService blacklistedTokenService;
    private final RestaurantRepository restaurantRepository;

    public JWTRequestFilter(JWTService jwtService, UserRepository userRepository, AdminRepository adminRepository,
                            BlacklistedTokenService blacklistedTokenService, RestaurantRepository restaurantRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.blacklistedTokenService = blacklistedTokenService;
        this.restaurantRepository = restaurantRepository;
    }

    private UsernamePasswordAuthenticationToken checkToken(String tokenHeader) {
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);

            if (blacklistedTokenService.isTokenBlacklisted(token)) {
                return null;
            }

            Long accountID = jwtService.getAccountID(token);
            String role = jwtService.getRoleFromToken(token);

            if ("ROLE_USER".equals(role)) {
                return userRepository.findLocalUserById(accountID)
                        .map(user -> new UsernamePasswordAuthenticationToken(
                                user, null, List.of(new SimpleGrantedAuthority(role))))
                        .orElse(null);
            }
            else if ("ROLE_RESTAURANT".equals(role)) {
                return restaurantRepository.findRestaurantById(accountID)
                        .map(restaurant -> new UsernamePasswordAuthenticationToken(
                                restaurant, null, List.of(new SimpleGrantedAuthority(role))))
                        .orElse(null);
            }
            else if ("ROLE_ADMIN".equals(role)) {
                return adminRepository.findAdminById(accountID)
                        .map(admin -> new UsernamePasswordAuthenticationToken(
                                admin, null, List.of(new SimpleGrantedAuthority(role))))
                        .orElse(null);
            }
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String tokenHeader = request.getHeader("Authorization");
        UsernamePasswordAuthenticationToken authentication = checkToken(tokenHeader);

        if (authentication != null) {
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            SecurityContextHolder.clearContext();
            if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token is invalid or blacklisted, please login again.");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        SimpMessageType messageType =
                (SimpMessageType) message.getHeaders().get("simpMessageType");
        if (messageType == SimpMessageType.SUBSCRIBE ||
                messageType == SimpMessageType.CONNECT ||
                messageType == SimpMessageType.MESSAGE) {
            // Type-safe extraction of native headers
            Object nativeHeadersObj = message.getHeaders().get("nativeHeaders");
            if (nativeHeadersObj instanceof Map<?, ?> nativeHeaders) {
                @SuppressWarnings("unchecked")
                List<String> authTokenList = (List<String>) nativeHeaders.get("Authorization");
                if (authTokenList != null && !authTokenList.isEmpty()) {
                    String tokenHeader = authTokenList.get(0);
                    UsernamePasswordAuthenticationToken authentication = checkToken(tokenHeader);
                    if (authentication != null) {
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        SecurityContextHolder.clearContext();
                    }
                }
            }
        }
        return message;
    }
}