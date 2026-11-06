package com.chat.app.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chat.app.dto.request.SendMessageDTO;
import com.chat.app.dto.response.MessageResponseDTO;
import com.chat.app.dto.response.UserResponseDTO;
import com.chat.app.service.MessageService;
import com.chat.app.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    // Get messages of a conversation
    @GetMapping("/{conversationId}")
    public ResponseEntity<Page<MessageResponseDTO>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        UserResponseDTO currentUser =
                userService.getUserByEmail(authentication.getName());

        Page<MessageResponseDTO> messages =
                messageService.getMessages(
                        currentUser.getId(),
                        conversationId,
                        page,
                        size
                );

        return ResponseEntity.ok(messages);
    }
    
 // Send message
    @PostMapping
    public ResponseEntity<MessageResponseDTO> sendMessage(
            @RequestBody SendMessageDTO request,
            Authentication authentication) {

        UserResponseDTO currentUser =
                userService.getUserByEmail(authentication.getName());

        MessageResponseDTO response =
                messageService.sendMessage(
                        currentUser.getId(),
                        request
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            Authentication authentication) {

        UserResponseDTO currentUser =
                userService.getUserByEmail(authentication.getName());

        messageService.deleteMessage(currentUser.getId(), messageId);

        return ResponseEntity.noContent().build();
    }
    
 // Hide received message for current user only
    @DeleteMapping("/{messageId}/hide")
    public ResponseEntity<Void> hideMessage(
            @PathVariable Long messageId,
            Authentication authentication) {

        UserResponseDTO currentUser =
                userService.getUserByEmail(authentication.getName());

        messageService.hideMessage(
                currentUser.getId(),
                messageId
        );

        return ResponseEntity.noContent().build();
    }
    
}
