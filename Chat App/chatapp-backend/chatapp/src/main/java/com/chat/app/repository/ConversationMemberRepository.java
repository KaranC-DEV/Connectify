package com.chat.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chat.app.entity.ConversationMember;

public interface ConversationMemberRepository
extends JpaRepository<ConversationMember, Long> {

List<ConversationMember> findByUser_Id(Long userId);

List<ConversationMember> findByConversation_Id(Long conversationId);

Optional<ConversationMember> findByConversation_IdAndUser_Id(
    Long conversationId, Long userId);

boolean existsByConversation_IdAndUser_Id(
    Long conversationId, Long userId);

void deleteByConversation_IdAndUser_Id(
    Long conversationId, Long userId);


}