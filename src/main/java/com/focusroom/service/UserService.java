package com.focusroom.service;

import com.focusroom.dto.ChangePasswordDto;
import com.focusroom.dto.UserProfileDto;
import com.focusroom.dto.UserRegistrationDto;
import com.focusroom.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {

    User registerUser(UserRegistrationDto dto);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    User getUserById(Long id);

    User updateProfile(Long userId, UserProfileDto dto);

    void changePassword(Long userId, ChangePasswordDto dto);

    Page<User> getAllUsers(String search, Pageable pageable);

    void toggleAccountStatus(Long userId);

    long countTotalUsers();

    long countActiveUsers();
}
