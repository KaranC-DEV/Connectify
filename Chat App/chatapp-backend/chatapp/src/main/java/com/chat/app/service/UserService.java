package com.chat.app.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chat.app.dto.request.RegisterRequestDTO;
import com.chat.app.dto.response.UserResponseDTO;
import com.chat.app.entity.User;
import com.chat.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
	private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Register a new user
    public UserResponseDTO registerUser(RegisterRequestDTO request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(
                    "Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Email already exists");
        }

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        // Encrypt password before saving
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        User savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }

    // Find user by ID
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                    new RuntimeException("User not found"));

        return mapToUserResponse(user);
    }

    // Search users by username
    @Transactional(readOnly = true)
    public List<UserResponseDTO> searchUsers(String username) {

        return userRepository
                .findByUsernameContainingIgnoreCase(username)
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    // Convert User Entity into UserResponseDTO
    private UserResponseDTO mapToUserResponse(User user) {
        return modelMapper.map(user, UserResponseDTO.class);
    }
    
    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return modelMapper.map(user, UserResponseDTO.class);
    }
}
