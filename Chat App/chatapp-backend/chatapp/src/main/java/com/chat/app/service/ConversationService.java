package com.chat.app.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chat.app.dto.request.CreateGroupRequestDTO;
import com.chat.app.dto.response.ConversationResponseDTO;
import com.chat.app.dto.response.UserResponseDTO;
import com.chat.app.entity.Conversation;
import com.chat.app.entity.ConversationMember;
import com.chat.app.entity.User;
import com.chat.app.enums.ConversationMemberRole;
import com.chat.app.enums.ConversationType;
import com.chat.app.repository.ConversationMemberRepository;
import com.chat.app.repository.ConversationRepository;
import com.chat.app.repository.UserConnectionRepository;
import com.chat.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ConversationService {
	
	private final ModelMapper modelMapper;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final UserConnectionRepository connectionRepository;

    // Create or retrieve private conversation
    public ConversationResponseDTO createPrivateConversation(
            Long userId, Long otherUserId) {

        if (userId.equals(otherUserId)) {
            throw new IllegalArgumentException(
                    "You cannot create a conversation with yourself");
        }

        User user = getUser(userId);
        User otherUser = getUser(otherUserId);

        // Check accepted connection
        boolean connected =
                connectionRepository
                    .existsByUser_IdAndConnectedUser_Id(
                            userId, otherUserId)
                || connectionRepository
                    .existsByUser_IdAndConnectedUser_Id(
                            otherUserId, userId);

        if (!connected) {
            throw new IllegalArgumentException(
                    "You must be connected before starting a private chat");
        }

        // Check whether private conversation already exists
        List<ConversationMember> existingMembers =
                memberRepository.findByUser_Id(userId);

        for (ConversationMember member : existingMembers) {

            Conversation conversation = member.getConversation();

            if (conversation.getType() == ConversationType.PRIVATE) {

                boolean hasOtherUser =
                        memberRepository
                            .existsByConversation_IdAndUser_Id(
                                    conversation.getId(), otherUserId);

                if (hasOtherUser) {
                    return mapToResponse(conversation);
                }
            }
        }

        // Create new private conversation
        Conversation conversation = new Conversation();

        conversation.setType(ConversationType.PRIVATE);
        conversation.setCreatedBy(user);

        Conversation savedConversation =
                conversationRepository.save(conversation);

        // Add first user
        addMember(
                savedConversation,
                user,
                ConversationMemberRole.MEMBER
        );

        // Add second user
        addMember(
                savedConversation,
                otherUser,
                ConversationMemberRole.MEMBER
        );

        return mapToResponse(savedConversation);
    }

    // Create group conversation
    public ConversationResponseDTO createGroup(
            Long creatorId, CreateGroupRequestDTO request) {

        if (request.getName() == null
                || request.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Group name cannot be empty");
        }

        if (request.getMemberIds() == null) {
            throw new IllegalArgumentException(
                    "Group members are required");
        }

        User creator = getUser(creatorId);

        // Remove duplicate IDs and include creator
        Set<Long> memberIds =
                new LinkedHashSet<>(request.getMemberIds());

        memberIds.add(creatorId);

        // Create group
        Conversation conversation = new Conversation();

        conversation.setType(ConversationType.GROUP);
        conversation.setName(request.getName());
        conversation.setCreatedBy(creator);

        Conversation savedConversation =
                conversationRepository.save(conversation);

        // Add members
        for (Long memberId : memberIds) {

            User member = getUser(memberId);

            ConversationMemberRole role =
                    memberId.equals(creatorId)
                    ? ConversationMemberRole.ADMIN
                    : ConversationMemberRole.MEMBER;

            addMember(savedConversation, member, role);
        }

        return mapToResponse(savedConversation);
    }

    // Get all conversations of a user
    @Transactional(readOnly = true)
    public List<ConversationResponseDTO> getUserConversations(
            Long userId) {

        getUser(userId);

        List<ConversationMember> memberships =
                memberRepository.findByUser_Id(userId);

        return memberships.stream()
                .map(ConversationMember::getConversation)
                .map(this::mapToResponse)
                .toList();
    }

    // Add member to conversation
    private void addMember(
            Conversation conversation,
            User user,
            ConversationMemberRole role) {

        ConversationMember member = new ConversationMember();

        member.setConversation(conversation);
        member.setUser(user);
        member.setRole(role);

        memberRepository.save(member);
    }

    // Find user
    private User getUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    // Convert Entity into DTO
    private ConversationResponseDTO mapToResponse(
            Conversation conversation) {

        ConversationResponseDTO response =
                modelMapper.map(
                        conversation,
                        ConversationResponseDTO.class
                );

        List<UserResponseDTO> participants =
                memberRepository
                        .findByConversation_Id(conversation.getId())
                        .stream()
                        .map(member -> modelMapper.map(
                                member.getUser(),
                                UserResponseDTO.class
                        ))
                        .toList();

        response.setParticipants(participants);

        return response;
    }
}
