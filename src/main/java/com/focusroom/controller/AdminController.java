package com.focusroom.controller;

import com.focusroom.entity.StudyRoom;
import com.focusroom.entity.User;
import com.focusroom.service.StudyRoomService;
import com.focusroom.service.StudySessionService;
import com.focusroom.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final StudyRoomService roomService;
    private final StudySessionService sessionService;

    public AdminController(UserService userService, StudyRoomService roomService, StudySessionService sessionService) {
        this.userService = userService;
        this.roomService = roomService;
        this.sessionService = sessionService;
    }

    @GetMapping
    public String adminDashboard(@RequestParam(value = "userPage", defaultValue = "0") int userPage,
                                 @RequestParam(value = "roomPage", defaultValue = "0") int roomPage,
                                 @RequestParam(value = "search", required = false) String search,
                                 Model model) {
        Page<User> users = userService.getAllUsers(search, PageRequest.of(userPage, 10));
        Page<StudyRoom> rooms = roomService.getAllRoomsForAdmin(PageRequest.of(roomPage, 10));

        long totalUsers = userService.countTotalUsers();
        long activeUsers = userService.countActiveUsers();
        long totalRooms = roomService.countTotalRooms();
        long activeRooms = roomService.countActiveRooms();
        long activeSessions = sessionService.countActiveSessionsAcrossPlatform();

        model.addAttribute("users", users);
        model.addAttribute("rooms", rooms);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("activeRooms", activeRooms);
        model.addAttribute("activeSessions", activeSessions);
        model.addAttribute("searchQuery", search);

        return "admin/dashboard";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleAccountStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/rooms/{id}/archive")
    public String adminArchiveRoom(@PathVariable("id") Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        User admin = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        try {
            roomService.archiveRoom(id, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Room archived successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/rooms/{id}/delete")
    public String adminDeleteRoom(@PathVariable("id") Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User admin = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        try {
            roomService.deleteRoom(id, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Room permanently deleted.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin";
    }
}
