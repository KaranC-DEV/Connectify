package com.chat.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chat.app.entity.UserConnection;

public interface UserConnectionRepository
extends JpaRepository<UserConnection, Long> {

List<UserConnection> findByUser_Id(Long userId);

Optional<UserConnection> findByUser_IdAndConnectedUser_Id(
    Long userId, Long connectedUserId);

boolean existsByUser_IdAndConnectedUser_Id(
    Long userId, Long connectedUserId);
}
