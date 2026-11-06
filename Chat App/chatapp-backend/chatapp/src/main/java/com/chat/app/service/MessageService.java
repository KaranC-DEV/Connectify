package com.chat.app.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chat.app.dto.request.SendMessageDTO;
import com.chat.app.dto.response.MessageResponseDTO;
import com.chat.app.entity.Conversation;
import com.chat.app.entity.Message;
import com.chat.app.entity.MessageHiddenForUser;
import com.chat.app.entity.User;
import com.chat.app.repository.ConversationMemberRepository;
import com.chat.app.repository.ConversationRepository;
import com.chat.app.repository.MessageHiddenForUserRepository;
import com.chat.app.repository.MessageRepository;
import com.chat.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {
	private final MessageHiddenForUserRepository hiddenMessageRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    // Send and save message
    public MessageResponseDTO sendMessage(
            Long senderId, SendMessageDTO request) {

        if (request.getContent() == null
                || request.getContent().isBlank()) {
            throw new IllegalArgumentException(
                    "Message cannot be empty");
        }

        Long conversationId = request.getConversationId();

        // Verify sender
        User sender = userRepository.findById(senderId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Verify conversation
        Conversation conversation =
                conversationRepository.findById(conversationId)
                .orElseThrow(() ->
                        new RuntimeException("Conversation not found"));

        // Verify conversation membership
        boolean isMember = memberRepository
                .existsByConversation_IdAndUser_Id(
                        conversationId, senderId);

        if (!isMember) {
            throw new IllegalArgumentException(
                    "You are not a member of this conversation");
        }

        // Create message
        Message message = new Message();

        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(request.getContent().trim());

        // Save message
        Message savedMessage = messageRepository.save(message);

        return modelMapper.map(
                savedMessage, MessageResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getMessages(
            Long userId,
            Long conversationId,
            int page,
            int size) {

        boolean isMember = memberRepository
                .existsByConversation_IdAndUser_Id(
                        conversationId, userId);

        if (!isMember) {
            throw new IllegalArgumentException(
                    "You cannot access this conversation");
        }

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Invalid pagination values");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Message> messages = messageRepository
                .findByConversation_IdOrderBySentAtDesc(
                        conversationId, pageable);

        // Get IDs of messages on this page
        List<Long> messageIds = messages.getContent()
                .stream()
                .map(Message::getId)
                .toList();

        // Find messages hidden by this particular user
        Set<Long> hiddenMessageIds = hiddenMessageRepository
                .findByUser_IdAndMessage_IdIn(userId, messageIds)
                .stream()
                .map(hidden -> hidden.getMessage().getId())
                .collect(Collectors.toSet());

        return messages.map(message -> {
            MessageResponseDTO dto = modelMapper.map(
                    message, MessageResponseDTO.class);

            dto.setDeletedForCurrentUser(
                    hiddenMessageIds.contains(message.getId()));

            return dto;
        });
    }
    
    
    public void deleteMessage(Long userId, Long messageId) {

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() ->
                        new RuntimeException("Message not found"));

        if (!message.getSender().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You can only delete your own messages");
        }

        message.setDeleted(true);
        message.setContent("This message was deleted");

        messageRepository.save(message);
    }
    
    
    public void hideMessage(Long userId, Long messageId) {

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() ->
                        new RuntimeException("Message not found"));

        Long conversationId = message.getConversation().getId();

        // Verify current user belongs to this conversation
        boolean isMember = memberRepository
                .existsByConversation_IdAndUser_Id(
                        conversationId, userId);

        if (!isMember) {
            throw new IllegalArgumentException(
                    "You are not a member of this conversation");
        }

        // Only received messages can be hidden using this method
        if (message.getSender().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You cannot hide your own message using this operation");
        }

        // Avoid duplicate records
        boolean alreadyHidden = hiddenMessageRepository
                .existsByMessage_IdAndUser_Id(messageId, userId);

        if (!alreadyHidden) {
            MessageHiddenForUser hidden = new MessageHiddenForUser();

            hidden.setMessage(message);

            User user = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new RuntimeException("User not found"));

            hidden.setUser(user);

            hiddenMessageRepository.save(hidden);
        }
    }
}
