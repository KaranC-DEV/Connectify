package com.chat.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.chat.app.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversation_IdOrderBySentAtDesc(
            Long conversationId,
            Pageable pageable
    );
}
