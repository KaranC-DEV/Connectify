package com.chat.app.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chat.app.dto.request.ConnectionRequestDTO;
import com.chat.app.dto.response.ConnectionResponseDTO;
import com.chat.app.dto.response.UserResponseDTO;
import com.chat.app.entity.ConnectionRequest;
import com.chat.app.entity.User;
import com.chat.app.entity.UserConnection;
import com.chat.app.enums.ConnectionRequestStatus;
import com.chat.app.repository.ConnectionRequestRepository;
import com.chat.app.repository.UserConnectionRepository;
import com.chat.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ConnectionService {
	private final ModelMapper modelMapper;
    private final ConnectionRequestRepository requestRepository;
    private final UserConnectionRepository connectionRepository;
    private final UserRepository userRepository;

    // Send connection request
    public ConnectionResponseDTO sendRequest(
            Long senderId, ConnectionRequestDTO requestDTO) {

        Long receiverId = requestDTO.getReceiverId();

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException(
                    "You cannot send a request to yourself");
        }

        User sender = getUser(senderId);
        User receiver = getUser(receiverId);

        // Check existing friendship in both directions
        if (isConnected(senderId, receiverId)) {
            throw new IllegalArgumentException(
                    "Users are already connected");
        }

        // Check pending request in either direction
        if (hasPendingRequest(senderId, receiverId)) {
            throw new IllegalArgumentException(
                    "A pending connection request already exists");
        }

        ConnectionRequest request = new ConnectionRequest();

        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(ConnectionRequestStatus.PENDING);

        ConnectionRequest savedRequest =
                requestRepository.save(request);

        return mapToResponse(savedRequest);
    }

    // Accept connection request
    public ConnectionResponseDTO acceptRequest(
            Long requestId, Long receiverId) {

        ConnectionRequest request = requestRepository
                .findByIdAndReceiver_Id(requestId, receiverId)
                .orElseThrow(() -> new RuntimeException(
                        "Connection request not found"));

        if (request.getStatus() != ConnectionRequestStatus.PENDING) {
            throw new IllegalArgumentException(
                    "This request is no longer pending");
        }

        Long senderId = request.getSender().getId();

        if (isConnected(senderId, receiverId)) {
            throw new IllegalArgumentException(
                    "Users are already connected");
        }

        // Store friendship in canonical ID order
        User firstUser;
        User secondUser;

        if (senderId < receiverId) {
            firstUser = request.getSender();
            secondUser = request.getReceiver();
        } else {
            firstUser = request.getReceiver();
            secondUser = request.getSender();
        }

        UserConnection connection = new UserConnection();
        connection.setUser(firstUser);
        connection.setConnectedUser(secondUser);

        connectionRepository.save(connection);

        request.setStatus(ConnectionRequestStatus.ACCEPTED);

        return mapToResponse(request);
    }

    // Reject connection request
    public ConnectionResponseDTO rejectRequest(
            Long requestId, Long receiverId) {

        ConnectionRequest request = requestRepository
                .findByIdAndReceiver_Id(requestId, receiverId)
                .orElseThrow(() -> new RuntimeException(
                        "Connection request not found"));

        if (request.getStatus() != ConnectionRequestStatus.PENDING) {
            throw new IllegalArgumentException(
                    "This request is no longer pending");
        }

        request.setStatus(ConnectionRequestStatus.REJECTED);

        return mapToResponse(request);
    }

    // Get received pending requests
    @Transactional(readOnly = true)
    public List<ConnectionResponseDTO> getReceivedPendingRequests(
            Long userId) {

        return requestRepository
                .findByReceiver_IdAndStatus(
                        userId, ConnectionRequestStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get sent pending requests
    @Transactional(readOnly = true)
    public List<ConnectionResponseDTO> getSentRequests(
            Long userId) {

        return requestRepository
                .findBySender_IdAndStatus(
                        userId, ConnectionRequestStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get connected users
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getConnections(Long userId) {

        return connectionRepository.findAll()
                .stream()
                .filter(connection ->
                        connection.getUser().getId().equals(userId)
                        || connection.getConnectedUser().getId()
                                .equals(userId))
                .map(connection -> {
                    User connectedUser =
                            connection.getUser().getId().equals(userId)
                            ? connection.getConnectedUser()
                            : connection.getUser();

                    return mapToUserResponse(connectedUser);
                })
                .toList();
    }

    // Check whether users are already connected
    private boolean isConnected(Long firstId, Long secondId) {

        return connectionRepository
                .existsByUser_IdAndConnectedUser_Id(
                        firstId, secondId)
                || connectionRepository
                .existsByUser_IdAndConnectedUser_Id(
                        secondId, firstId);
    }

    // Check pending requests in both directions
    private boolean hasPendingRequest(
            Long firstId, Long secondId) {

        return requestRepository
                .existsBySender_IdAndReceiver_IdAndStatus(
                        firstId, secondId,
                        ConnectionRequestStatus.PENDING)
                || requestRepository
                .existsBySender_IdAndReceiver_IdAndStatus(
                        secondId, firstId,
                        ConnectionRequestStatus.PENDING);
    }

    // Find user
    private User getUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    // Convert ConnectionRequest entity into DTO
    private ConnectionResponseDTO mapToResponse(
            ConnectionRequest request) {

        return modelMapper.map(
                request, ConnectionResponseDTO.class);
    }

    // Convert User entity into DTO
    private UserResponseDTO mapToUserResponse(User user) {

        return modelMapper.map(user, UserResponseDTO.class);
    }
}
