package com.chat.app.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chat.app.dto.response.UserResponseDTO;
import com.chat.app.entity.Conversation;
import com.chat.app.entity.ConversationMember;
import com.chat.app.entity.User;
import com.chat.app.enums.ConversationMemberRole;
import com.chat.app.enums.ConversationType;
import com.chat.app.repository.ConversationMemberRepository;
import com.chat.app.repository.ConversationRepository;
import com.chat.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    // Add a member to a group
    public void addMember(
            Long groupId, Long adminId, Long userId) {

        Conversation group = getGroup(groupId);

        verifyAdmin(groupId, adminId);

        User user = getUser(userId);

        if (memberRepository.existsByConversation_IdAndUser_Id(
                groupId, userId)) {

            throw new IllegalArgumentException(
                    "User is already a group member");
        }

        ConversationMember member = new ConversationMember();

        member.setConversation(group);
        member.setUser(user);
        member.setRole(ConversationMemberRole.MEMBER);

        memberRepository.save(member);
    }

    // Remove a member from a group
    public void removeMember(
            Long groupId, Long adminId, Long userId) {

        Conversation group = getGroup(groupId);

        verifyAdmin(groupId, adminId);

        ConversationMember member = memberRepository
                .findByConversation_IdAndUser_Id(
                        groupId, userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Group member not found"));

        if (member.getRole() == ConversationMemberRole.ADMIN) {
            throw new IllegalArgumentException(
                    "An admin cannot be removed directly");
        }

        memberRepository.delete(member);
    }

    // Member leaves a group
    public void leaveGroup(Long groupId, Long userId) {

        Conversation group = getGroup(groupId);

        ConversationMember member = memberRepository
                .findByConversation_IdAndUser_Id(
                        groupId, userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "You are not a group member"));

        if (member.getRole() == ConversationMemberRole.ADMIN) {

            long adminCount = memberRepository
                    .findByConversation_Id(groupId)
                    .stream()
                    .filter(m -> m.getRole()
                            == ConversationMemberRole.ADMIN)
                    .count();

            if (adminCount == 1) {
                throw new IllegalArgumentException(
                        "Transfer admin role before leaving the group");
            }
        }

        memberRepository.delete(member);
    }

    // Get all group members
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getGroupMembers(Long groupId) {

        getGroup(groupId);

        return memberRepository
                .findByConversation_Id(groupId)
                .stream()
                .map(ConversationMember::getUser)
                .map(user -> modelMapper.map(
                        user, UserResponseDTO.class))
                .toList();
    }

    // Verify group exists and is a GROUP conversation
    private Conversation getGroup(Long groupId) {

        Conversation group = conversationRepository
                .findById(groupId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Group not found"));

        if (group.getType() != ConversationType.GROUP) {
            throw new IllegalArgumentException(
                    "This conversation is not a group");
        }

        return group;
    }

    // Verify admin permissions
    private void verifyAdmin(Long groupId, Long adminId) {

        ConversationMember member = memberRepository
                .findByConversation_IdAndUser_Id(
                        groupId, adminId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "You are not a group member"));

        if (member.getRole() != ConversationMemberRole.ADMIN) {
            throw new IllegalArgumentException(
                    "Only group admins can perform this action");
        }
    }

    // Find user
    private User getUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }
}
