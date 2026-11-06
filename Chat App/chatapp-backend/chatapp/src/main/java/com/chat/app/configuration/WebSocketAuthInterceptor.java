package com.chat.app.configuration;

import java.security.Principal;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.chat.app.entity.User;
import com.chat.app.repository.ConversationMemberRepository;
import com.chat.app.repository.UserRepository;
import com.chat.app.security.CustomUserDetailsService;
import com.chat.app.security.JwtService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final ConversationMemberRepository memberRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // Authenticate WebSocket connection
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader =
                    accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Missing JWT token");
            }

            String token = authHeader.substring(7);

            if (!jwtService.validateToken(token)) {
                throw new IllegalArgumentException("Invalid JWT token");
            }

            String email = jwtService.getUserName(token);

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

            accessor.setUser(authentication);
        }

        // Authorize conversation SEND and SUBSCRIBE
        if (StompCommand.SEND.equals(accessor.getCommand())
                || StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            Principal principal = accessor.getUser();

            if (principal == null) {
                throw new IllegalArgumentException(
                        "Unauthenticated WebSocket connection");
            }

            String destination = accessor.getDestination();

            if (destination == null) {
                throw new IllegalArgumentException("Destination is required");
            }

            Long conversationId = extractConversationId(
                    destination, accessor.getCommand());

            String email = principal.getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new IllegalArgumentException("User not found"));

            boolean isMember =
                    memberRepository.existsByConversation_IdAndUser_Id(
                            conversationId, user.getId());

            if (!isMember) {
                throw new IllegalArgumentException(
                        "You are not a member of this conversation");
            }
        }

        return message;
    }

    private Long extractConversationId(
            String destination, StompCommand command) {

        String prefix = command == StompCommand.SEND
                ? "/app/conversations/"
                : "/topic/conversations/";

        if (!destination.startsWith(prefix)) {
            throw new IllegalArgumentException(
                    "Invalid conversation destination");
        }

        String remaining = destination.substring(prefix.length());

        if (command == StompCommand.SEND
                && !remaining.endsWith("/sendMessage")) {
            throw new IllegalArgumentException(
                    "Invalid message destination");
        }

        String conversationId = command == StompCommand.SEND
                ? remaining.substring(0, remaining.indexOf("/sendMessage"))
                : remaining;

        try {
            return Long.parseLong(conversationId);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Invalid conversation ID");
        }
    }
}