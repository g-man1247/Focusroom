package com.focusroom.service.impl;

import com.focusroom.dto.ChangePasswordDto;
import com.focusroom.dto.UserProfileDto;
import com.focusroom.dto.UserRegistrationDto;
import com.focusroom.entity.User;
import com.focusroom.entity.enums.AccountStatus;
import com.focusroom.entity.enums.Role;
import com.focusroom.exception.DuplicateResourceException;
import com.focusroom.exception.InvalidOperationException;
import com.focusroom.exception.ResourceNotFoundException;
import com.focusroom.repository.UserRepository;
import com.focusroom.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User registerUser(UserRegistrationDto dto) {
        if (!dto.isPasswordMatching()) {
            throw new InvalidOperationException("Passwords do not match");
        }

        if (userRepository.existsByUsername(dto.getUsername().trim())) {
            throw new DuplicateResourceException("Username '" + dto.getUsername() + "' is already taken");
        }

        if (userRepository.existsByEmail(dto.getEmail().trim().toLowerCase())) {
            throw new DuplicateResourceException("An account with email '" + dto.getEmail() + "' already exists");
        }

        User user = new User();
        user.setUsername(dto.getUsername().trim());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName().trim());
        
        // If this is the very first registered user in the database, promote to ADMIN for convenience!
        if (userRepository.count() == 0) {
            user.setRole(Role.ROLE_ADMIN);
        } else {
            user.setRole(Role.ROLE_USER);
        }
        user.setAccountStatus(AccountStatus.ACTIVE);

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, UserProfileDto dto) {
        User user = getUserById(userId);

        if (!user.getEmail().equalsIgnoreCase(dto.getEmail().trim())) {
            if (userRepository.existsByEmail(dto.getEmail().trim().toLowerCase())) {
                throw new DuplicateResourceException("Email is already taken by another account");
            }
            user.setEmail(dto.getEmail().trim().toLowerCase());
        }

        user.setFullName(dto.getFullName().trim());
        user.setBio(dto.getBio() != null ? dto.getBio().trim() : null);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordDto dto) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidOperationException("Current password does not match our records");
        }

        if (!dto.isPasswordMatching()) {
            throw new InvalidOperationException("New password confirmation does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return userRepository.searchUsers(search.trim(), pageable);
        }
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public void toggleAccountStatus(Long userId) {
        User user = getUserById(userId);
        if (user.getRole() == Role.ROLE_ADMIN) {
            throw new InvalidOperationException("Cannot deactivate an Administrator account");
        }
        if (user.getAccountStatus() == AccountStatus.ACTIVE) {
            user.setAccountStatus(AccountStatus.SUSPENDED);
        } else {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalUsers() {
        return userRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveUsers() {
        return userRepository.countByAccountStatus(AccountStatus.ACTIVE);
    }
}
