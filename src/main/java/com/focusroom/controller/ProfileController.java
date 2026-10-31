package com.focusroom.controller;

import com.focusroom.dto.ChangePasswordDto;
import com.focusroom.dto.UserProfileDto;
import com.focusroom.entity.User;
import com.focusroom.exception.DuplicateResourceException;
import com.focusroom.exception.InvalidOperationException;
import com.focusroom.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String profilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();

        if (!model.containsAttribute("userProfileDto")) {
            model.addAttribute("userProfileDto", new UserProfileDto(user.getUsername(), user.getEmail(), user.getFullName(), user.getBio()));
        }
        if (!model.containsAttribute("changePasswordDto")) {
            model.addAttribute("changePasswordDto", new ChangePasswordDto());
        }

        model.addAttribute("profileUser", user);
        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("userProfileDto") UserProfileDto dto,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please fix errors in profile form.");
            return "redirect:/profile";
        }

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        try {
            userService.updateProfile(user.getId(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (DuplicateResourceException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("changePasswordDto") ChangePasswordDto dto,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters.");
            return "redirect:/profile";
        }

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        try {
            userService.changePassword(user.getId(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
        } catch (InvalidOperationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/profile";
    }
}
