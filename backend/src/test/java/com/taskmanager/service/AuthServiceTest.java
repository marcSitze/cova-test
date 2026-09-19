package com.taskmanager.service;

import com.taskmanager.dto.request.LoginRequest;
import com.taskmanager.dto.request.RegisterRequest;
import com.taskmanager.dto.response.AuthResponse;
import com.taskmanager.dto.response.UserResponse;
import com.taskmanager.entity.UserEntity;
import com.taskmanager.exception.EmailAlreadyExistsException;
import com.taskmanager.mapper.UserMapper;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.JwtTokenProvider;
import com.taskmanager.security.UserPrincipal;
import com.taskmanager.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserEntity userEntity;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userEntity = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded_password")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userResponse = new UserResponse(1L, "test@example.com", LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("Inscrire un nouvel utilisateur valide doit retourner un AuthResponse avec un jeton JWT")
    void register_Success() {
        RegisterRequest request = new RegisterRequest("test@example.com", "Password123!");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded_password");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(jwtTokenProvider.generateTokenFromUser(1L, "test@example.com")).thenReturn("mocked_jwt_token");
        when(jwtTokenProvider.getJwtExpirationInMs()).thenReturn(86400000L);
        when(userMapper.toUserResponse(any(UserEntity.class))).thenReturn(userResponse);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mocked_jwt_token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals("test@example.com", response.user().email());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Inscrire un email déjà existant doit lever EmailAlreadyExistsException (409)")
    void register_EmailAlreadyExists_ThrowsException() {
        RegisterRequest request = new RegisterRequest("test@example.com", "Password123!");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Se connecter avec des identifiants valides doit retourner AuthResponse")
    void login_Success() {
        LoginRequest request = new LoginRequest("test@example.com", "Password123!");
        Authentication authentication = mock(Authentication.class);
        UserPrincipal userPrincipal = UserPrincipal.create(userEntity);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("mocked_jwt_token");
        when(jwtTokenProvider.getJwtExpirationInMs()).thenReturn(86400000L);
        when(userMapper.toUserResponse(userEntity)).thenReturn(userResponse);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mocked_jwt_token", response.token());
        assertEquals("test@example.com", response.user().email());
    }

    @Test
    @DisplayName("Se connecter avec des identifiants invalides doit lever BadCredentialsException")
    void login_BadCredentials_ThrowsException() {
        LoginRequest request = new LoginRequest("test@example.com", "wrong_password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}
