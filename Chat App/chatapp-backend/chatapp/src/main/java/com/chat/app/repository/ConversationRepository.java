package com.chat.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chat.app.entity.Conversation;
import com.chat.app.enums.ConversationType;

public interface ConversationRepository
extends JpaRepository<Conversation, Long> {

List<Conversation> findByType(ConversationType type);

List<Conversation> findByCreatedBy_Id(Long userId);
}
