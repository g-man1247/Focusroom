package com.focusroom.service;

import com.focusroom.dto.UserRegistrationDto;
import com.focusroom.entity.User;
import com.focusroom.entity.enums.Role;
import com.focusroom.exception.DuplicateResourceException;
import com.focusroom.exception.InvalidOperationException;
import com.focusroom.repository.UserRepository;
import com.focusroom.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegistrationDto registrationDto;

    @BeforeEach
    void setUp() {
        registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("alice");
        registrationDto.setEmail("alice@university.edu");
        registrationDto.setPassword("secret123");
        registrationDto.setConfirmPassword("secret123");
        registrationDto.setFullName("Alice Smith");
    }

    @Test
    @DisplayName("Should successfully register initial user with ROLE_ADMIN")
    void testRegisterFirstUserAsAdmin() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@university.edu")).thenReturn(false);
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$10$encodedPasswordHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = userService.registerUser(registrationDto);

        assertNotNull(registered);
        assertEquals("alice", registered.getUsername());
        assertEquals(Role.ROLE_ADMIN, registered.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when password confirmation does not match")
    void testRegisterPasswordMismatch() {
        registrationDto.setConfirmPassword("differentPass");

        assertThrows(InvalidOperationException.class, () -> userService.registerUser(registrationDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when username exists")
    void testRegisterDuplicateUsername() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.registerUser(registrationDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email exists")
    void testRegisterDuplicateEmail() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@university.edu")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.registerUser(registrationDto));
        verify(userRepository, never()).save(any(User.class));
    }
}
