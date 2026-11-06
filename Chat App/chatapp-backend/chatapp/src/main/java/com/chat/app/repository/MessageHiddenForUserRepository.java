package com.chat.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chat.app.entity.MessageHiddenForUser;

public interface MessageHiddenForUserRepository
extends JpaRepository<MessageHiddenForUser, Long> {

boolean existsByMessage_IdAndUser_Id(Long messageId, Long userId);

List<MessageHiddenForUser> findByUser_IdAndMessage_IdIn(
Long userId,
List<Long> messageIds
);
}
