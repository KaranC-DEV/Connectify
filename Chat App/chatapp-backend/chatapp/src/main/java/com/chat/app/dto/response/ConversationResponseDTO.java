package com.chat.app.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.chat.app.enums.ConversationType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponseDTO {

    private Long id;

    private ConversationType type;

    private String name;

    private UserResponseDTO createdBy;

    private List<UserResponseDTO> participants;
    private LocalDateTime createdAt;
}