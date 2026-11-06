package com.chat.app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chat.app.dto.response.UserResponseDTO;
import com.chat.app.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
	
	 private final UserService userService;


	// Get logged-in user's profile
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(
            Authentication authentication) {

        UserResponseDTO user =
                userService.getUserByEmail(authentication.getName());

        return ResponseEntity.ok(user);
    }

    // Search users by username
    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDTO>> searchUsers(
            @RequestParam String username,
            Authentication authentication) {

        List<UserResponseDTO> users =
                userService.searchUsers(username);

        // Exclude the currently logged-in user
        users = users.stream()
                .filter(user -> !user.getEmail()
                        .equalsIgnoreCase(authentication.getName()))
                .toList();

        return ResponseEntity.ok(users);
    }
}

