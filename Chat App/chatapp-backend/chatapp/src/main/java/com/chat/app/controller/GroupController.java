package com.chat.app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chat.app.dto.request.CreateGroupRequestDTO;
import com.chat.app.dto.response.ConversationResponseDTO;
import com.chat.app.dto.response.UserResponseDTO;
import com.chat.app.service.ConversationService;
import com.chat.app.service.GroupService;
import com.chat.app.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final ConversationService conversationService;
    private final GroupService groupService;
    private final UserService userService;

    // Create group
    @PostMapping
    public ResponseEntity<ConversationResponseDTO> createGroup(
            @RequestBody CreateGroupRequestDTO request,
            Authentication authentication) {

        UserResponseDTO currentUser =
                userService.getUserByEmail(authentication.getName());

        ConversationResponseDTO response =
                conversationService.createGroup(
                        currentUser.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Add member to group
    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<String> addMember(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            Authentication authentication) {

        UserResponseDTO currentUser =
                userService.getUserByEmail(authentication.getName());

        groupService.addMember(
                groupId, currentUser.getId(), userId);

        return ResponseEntity.ok("Member added successfully");
    }

    // Remove member from group
    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<String> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            Authentication authentication) {

        UserResponseDTO currentUser =
                userService.getUserByEmail(authentication.getName());

        groupService.removeMember(
                groupId, currentUser.getId(), userId);

        return ResponseEntity.ok("Member removed successfully");
    }

    // Leave group
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<String> leaveGroup(
            @PathVariable Long groupId,
            Authentication authentication) {

        UserResponseDTO currentUser =
                userService.getUserByEmail(authentication.getName());

        groupService.leaveGroup(groupId, currentUser.getId());

        return ResponseEntity.ok("You left the group successfully");
    }

    // Get group members
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<UserResponseDTO>> getGroupMembers(
            @PathVariable Long groupId) {

        return ResponseEntity.ok(
                groupService.getGroupMembers(groupId));
    }
}