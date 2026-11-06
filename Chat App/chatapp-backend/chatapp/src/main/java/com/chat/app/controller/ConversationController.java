package com.chat.app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chat.app.dto.request.PrivateConversationRequestDTO;
import com.chat.app.dto.response.ConversationResponseDTO;
import com.chat.app.dto.response.UserResponseDTO;
import com.chat.app.service.ConversationService;
import com.chat.app.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final UserService userService;

    // Create or retrieve an existing private conversation
    @PostMapping("/private")
    public ResponseEntity<ConversationResponseDTO> createPrivateConversation(
            @RequestBody PrivateConversationRequestDTO request,
            Authentication authentication) {

        UserResponseDTO currentUser =
                userService.getUserByEmail(authentication.getName());

        ConversationResponseDTO response =
                conversationService.createPrivateConversation(
                        currentUser.getId(),
                        request.getOtherUserId()
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all conversations of logged-in user
    @GetMapping
    public ResponseEntity<List<ConversationResponseDTO>> getMyConversations(
            Authentication authentication) {

        UserResponseDTO currentUser =
                userService.getUserByEmail(authentication.getName());

        List<ConversationResponseDTO> conversations =
                conversationService.getUserConversations(currentUser.getId());

        return ResponseEntity.ok(conversations);
    }
}
