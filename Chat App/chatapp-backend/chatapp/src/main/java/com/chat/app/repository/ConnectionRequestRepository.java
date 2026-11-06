package com.chat.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chat.app.entity.ConnectionRequest;
import com.chat.app.enums.ConnectionRequestStatus;

public interface ConnectionRequestRepository
extends JpaRepository<ConnectionRequest, Long> {

List<ConnectionRequest> findByReceiver_IdAndStatus(
    Long receiverId, ConnectionRequestStatus status);

List<ConnectionRequest> findBySender_IdAndStatus(
    Long senderId, ConnectionRequestStatus status);

Optional<ConnectionRequest> findByIdAndReceiver_Id(
    Long requestId, Long receiverId);

boolean existsBySender_IdAndReceiver_IdAndStatus(
    Long senderId,
    Long receiverId,
    ConnectionRequestStatus status);




}