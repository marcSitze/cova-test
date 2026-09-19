package com.taskmanager.service.impl;

import com.taskmanager.dto.request.LoginRequest;
import com.taskmanager.dto.request.RegisterRequest;
import com.taskmanager.dto.response.AuthResponse;
import com.taskmanager.dto.response.UserResponse;
import com.taskmanager.entity.UserEntity;
import com.taskmanager.exception.EmailAlreadyExistsException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.mapper.UserMapper;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.JwtTokenProvider;
import com.taskmanager.security.UserPrincipal;
import com.taskmanager.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().toLowerCase().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            log.warn("Tentative d'inscription avec un email existant : {}", normalizedEmail);
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        UserEntity user = UserEntity.builder()
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.password()))
                .build();

        UserEntity savedUser = userRepository.save(user);
        log.info("Nouvel utilisateur inscrit avec succès, ID: {}", savedUser.getId());

        String token = jwtTokenProvider.generateTokenFromUser(savedUser.getId(), savedUser.getEmail());
        UserResponse userResponse = userMapper.toUserResponse(savedUser);
        long expiresInSeconds = jwtTokenProvider.getJwtExpirationInMs() / 1000;

        return new AuthResponse(token, expiresInSeconds, userResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().toLowerCase().trim();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UserEntity user = userRepository.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userPrincipal.getEmail()));

        String token = jwtTokenProvider.generateToken(authentication);
        UserResponse userResponse = userMapper.toUserResponse(user);
        long expiresInSeconds = jwtTokenProvider.getJwtExpirationInMs() / 1000;

        log.info("Connexion réussie pour l'utilisateur ID: {}", user.getId());
        return new AuthResponse(token, expiresInSeconds, userResponse);
    }
}
