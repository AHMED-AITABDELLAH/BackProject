package com.savorybites.backend.security;


import com.savorybites.backend.model.Admin;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.Restaurant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Map;
import java.util.Objects;

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
public class WebSocketConfig implements
        WebSocketMessageBrokerConfigurer {

    @Value("${app.frontend.url}")
    private String frontendURL;
    private final JWTRequestFilter jwtRequestFilter;
    private final ApplicationContext context;
    private static final AntPathMatcher  MATCHER = new AntPathMatcher();

    public WebSocketConfig(JWTRequestFilter jwtRequestFilter, ApplicationContext context) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.context = context;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        WebSocketMessageBrokerConfigurer.super.registerStompEndpoints(registry);
        registry.addEndpoint("/websocket")
                .setAllowedOriginPatterns("http://localhost:63342",frontendURL)
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }


    private AuthorizationManager<Message<?>> makeMessageAuthorizationManager() {
        MessageMatcherDelegatingAuthorizationManager.Builder messages =
                new MessageMatcherDelegatingAuthorizationManager.Builder();
        messages.
                simpDestMatchers("/topic/public/**").permitAll()
                .simpDestMatchers("/topic/user/**").hasRole("USER")
                .simpDestMatchers("/topic/admin/**").hasRole("ADMIN")
                .simpDestMatchers("/topic/restaurant/**").hasRole("RESTAURANT")
                .anyMessage().permitAll();
        return messages.build();
    }


    //from the client to the server (Inbound)


    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        WebSocketMessageBrokerConfigurer.super.configureClientInboundChannel(registration);

        AuthorizationManager<Message<?>> authorizationManager =
                makeMessageAuthorizationManager();

        AuthorizationChannelInterceptor authInterceptor =
                new AuthorizationChannelInterceptor(authorizationManager);

        AuthorizationEventPublisher publisher =
                new SpringAuthorizationEventPublisher(context);

        authInterceptor.setAuthorizationEventPublisher(publisher);

        registration.interceptors(jwtRequestFilter, authInterceptor,
                new RejectClientMessagesOnChannelsChannelInterceptor(),
                new DestinationLevelAuthorizationChannelInterceptor());
    }








    private static class RejectClientMessagesOnChannelsChannelInterceptor
            implements ChannelInterceptor {

        //Paths that do not allow client messages.
        private final String[] paths = new String[] {
                "/topic/**"
        };

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            if (Objects.equals(message.getHeaders().get("simpMessageType"), SimpMessageType.MESSAGE)) {
                String destination = (String) message.getHeaders().get(
                        "simpDestination");
                for (String path: paths) {
                    assert destination != null;
                    if (MATCHER.match(path, destination))
                        message = null;
                }
            }
            return message;
        }

    }


    private static class DestinationLevelAuthorizationChannelInterceptor implements ChannelInterceptor {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            if (message.getHeaders().get("simpMessageType") == SimpMessageType.SUBSCRIBE) {
                String destination = (String) message.getHeaders().get("simpDestination");
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                // All destination type matchers
                String userTopicMatcher = "/topic/user/{userId}/**";
                String adminTopicMatcher = "/topic/admin/{adminId}/**";
                String restaurantTopicMatcher = "/topic/restaurant/{restaurantId}/**";

                try {
                    assert destination != null;
                    if (MATCHER.match(userTopicMatcher, destination)) {
                        Map<String, String> params = MATCHER.extractUriTemplateVariables(userTopicMatcher, destination);
                        Long userId = Long.valueOf(params.get("userId"));
                        if (!hasAccess(authentication, "ROLE_USER", userId)) {
                            return null;
                        }
                    } else if (MATCHER.match(adminTopicMatcher, destination)) {
                        Map<String, String> params = MATCHER.extractUriTemplateVariables(adminTopicMatcher, destination);
                        Long adminId = Long.valueOf(params.get("adminId"));
                        if (!hasAccess(authentication, "ROLE_ADMIN", adminId)) {
                            return null;
                        }
                    } else if (MATCHER.match(restaurantTopicMatcher, destination)) {
                        Map<String, String> params = MATCHER.extractUriTemplateVariables(restaurantTopicMatcher, destination);
                        Long restaurantId = Long.valueOf(params.get("restaurantId"));
                        if (!hasAccess(authentication, "ROLE_RESTAURANT", restaurantId)) {
                            return null;
                        }
                    }
                } catch (NumberFormatException ex) {
                    return null;
                }
            }
            return message;
        }

        // Helper method to centralize access checks
        private boolean hasAccess(Authentication auth, String requiredRole, Long id) {
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }
            if (auth.getAuthorities().stream().noneMatch(
                    a -> a.getAuthority().equals(requiredRole))) {
                return false;
            }
            Object principal = auth.getPrincipal();
            // Add role-specific access checks here
            return switch (requiredRole) {
                case "ROLE_USER" ->
                    // Replace it with your user permission logic
                        (principal instanceof LocalUser user)
                                && user.getId().equals(id); // or userService.userHasPermissionToUser(user, id)
                case "ROLE_ADMIN" ->
                    // Replace it with your admin permission logic
                        (principal instanceof Admin admin)
                                && admin.getId().equals(id);
                case "ROLE_RESTAURANT" ->
                    // Replace it with your restaurant permission logic
                        (principal instanceof Restaurant restaurant)
                                && restaurant.getId().equals(id);
                default -> false;
            };
        }
    }

}
