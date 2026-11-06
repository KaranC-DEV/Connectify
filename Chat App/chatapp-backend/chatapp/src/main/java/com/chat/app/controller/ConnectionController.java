package com.chat.app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chat.app.dto.request.ConnectionRequestDTO;
import com.chat.app.dto.response.ConnectionResponseDTO;
import com.chat.app.dto.response.UserResponseDTO;
import com.chat.app.service.ConnectionService;
import com.chat.app.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;
    private final UserService userService;

    // Send connection request
    @PostMapping("/requests")
    public ResponseEntity<ConnectionResponseDTO> sendRequest(
            @RequestBody ConnectionRequestDTO requestDTO,
            Authentication authentication) {

        UserResponseDTO currentUser =
                userService.getUserByEmail(authentication.getName());

        ConnectionResponseDTO response =
                connectionService.sendRequest(
                        currentUser.getId(), requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get received pending requests
    @GetMapping("/requests/received")
    public ResponseEntity<List<ConnectionResponseDTO>> getReceivedRequests(
            Authentication authentication) {

        UserResponseDTO currentUser =
                userService.getUserByEmail(authentication.getName());

        List<ConnectionResponseDTO> requests =
                connectionService.getReceivedPendingRequests(
                        currentUser.getId());

        return ResponseEntity.ok(requests);
    }

    // Accept connection request
    @PutMapping("/requests/{requestId}/accept")
    public ResponseEntity<ConnectionResponseDTO> acceptRequest(
            @PathVariable Long requestId,
            Authentication authentication) {

        UserResponseDTO currentUser =
                userService.getUserByEmail(authentication.getName());

        ConnectionResponseDTO response =
                connectionService.acceptRequest(
                        requestId, currentUser.getId());

        return ResponseEntity.ok(response);
    }

    // Reject connection request
    @PutMapping("/requests/{requestId}/reject")
    public ResponseEntity<ConnectionResponseDTO> rejectRequest(
            @PathVariable Long requestId,
            Authentication authentication) {

        UserResponseDTO currentUser =
                userService.getUserByEmail(authentication.getName());

        ConnectionResponseDTO response =
                connectionService.rejectRequest(
                        requestId, currentUser.getId());

        return ResponseEntity.ok(response);
    }
    
 // Get all accepted connections
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getMyConnections(
            Authentication authentication) {

        UserResponseDTO currentUser =
                userService.getUserByEmail(authentication.getName());

        List<UserResponseDTO> connections =
                connectionService.getConnections(currentUser.getId());

        return ResponseEntity.ok(connections);
    }
    
}