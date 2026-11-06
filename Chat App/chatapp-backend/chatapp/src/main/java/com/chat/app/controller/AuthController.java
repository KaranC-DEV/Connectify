package com.chat.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chat.app.dto.request.LoginRequestDTO;
import com.chat.app.dto.request.RegisterRequestDTO;
import com.chat.app.dto.response.AuthResponseDTO;
import com.chat.app.dto.response.UserResponseDTO;
import com.chat.app.security.JwtService;
import com.chat.app.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(
            @RequestBody RegisterRequestDTO request) {

        UserResponseDTO response = userService.registerUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> loginUser(
            @RequestBody LoginRequestDTO request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = jwtService.generateToken(request.getEmail());

        UserResponseDTO user =
                userService.getUserByEmail(request.getEmail());

        AuthResponseDTO response = new AuthResponseDTO(
                token,
                "Bearer",
                user
        );

        return ResponseEntity.ok(response);
    }
}