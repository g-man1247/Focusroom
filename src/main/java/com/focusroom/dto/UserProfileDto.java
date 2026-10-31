package com.focusroom.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserProfileDto {

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Valid email is required")
    private String email;

    @NotBlank(message = "Full name cannot be blank")
    @Size(max = 100)
    private String fullName;

    @Size(max = 255)
    private String bio;

    public UserProfileDto() {
    }

    public UserProfileDto(String username, String email, String fullName, String bio) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.bio = bio;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
