package com.focusroom.controller;

import com.focusroom.entity.User;
import com.focusroom.service.NotificationService;
import com.focusroom.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserService userService;
    private final NotificationService notificationService;

    public GlobalControllerAdvice(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model, Principal principal, Authentication authentication) {
        if (principal != null) {
            userService.findByUsername(principal.getName()).ifPresent(currentUser -> {
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("unreadNotificationCount", notificationService.getUnreadCount(currentUser));
                model.addAttribute("isAdminUser", currentUser.isAdmin());
            });
        }
    }
}
