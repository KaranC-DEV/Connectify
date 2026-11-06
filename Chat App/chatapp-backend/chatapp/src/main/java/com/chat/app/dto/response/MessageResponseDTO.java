package com.chat.app.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDTO {

    private Long id;

    private UserResponseDTO sender;

    private String content;

    private LocalDateTime sentAt;
    private boolean deleted;
    
    private boolean deletedForCurrentUser;
}
