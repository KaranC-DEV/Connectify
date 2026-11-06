package com.chat.app.dto.response;

import java.time.LocalDateTime;

import com.chat.app.enums.ConnectionRequestStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionResponseDTO {

    private Long id;

    private UserResponseDTO sender;

    private UserResponseDTO receiver;

    private ConnectionRequestStatus status;

    private LocalDateTime createdAt;
}
