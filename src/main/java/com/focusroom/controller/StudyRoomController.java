package com.focusroom.controller;

import com.focusroom.dto.ChatMessageDto;
import com.focusroom.dto.CreateRoomDto;
import com.focusroom.entity.RoomMembership;
import com.focusroom.entity.StudyRoom;
import com.focusroom.entity.User;
import com.focusroom.exception.RoomFullException;
import com.focusroom.exception.UnauthorizedAccessException;
import com.focusroom.service.ChatMessageService;
import com.focusroom.service.StudyRoomService;
import com.focusroom.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/rooms")
public class StudyRoomController {

    private final StudyRoomService roomService;
    private final UserService userService;
    private final ChatMessageService chatMessageService;

    public StudyRoomController(StudyRoomService roomService, UserService userService, ChatMessageService chatMessageService) {
        this.roomService = roomService;
        this.userService = userService;
        this.chatMessageService = chatMessageService;
    }

    @GetMapping
    public String exploreRooms(@RequestParam(value = "search", required = false) String search, Model model) {
        List<StudyRoom> rooms;
        if (search != null && !search.isBlank()) {
            rooms = roomService.searchRooms(search.trim());
        } else {
            rooms = roomService.getPublicActiveRooms();
        }

        model.addAttribute("rooms", rooms);
        model.addAttribute("searchQuery", search);
        return "rooms/explore";
    }

    @GetMapping("/create")
    public String createRoomPage(Model model) {
        if (!model.containsAttribute("createRoomDto")) {
            model.addAttribute("createRoomDto", new CreateRoomDto());
        }
        return "rooms/create";
    }

    @PostMapping("/create")
    public String createRoom(@Valid @ModelAttribute("createRoomDto") CreateRoomDto dto,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            return "rooms/create";
        }

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        try {
            StudyRoom room = roomService.createRoom(dto, user);
            redirectAttributes.addFlashAttribute("successMessage", "Study room '" + room.getName() + "' created successfully!");
            return "redirect:/rooms/" + room.getId();
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "rooms/create";
        }
    }

    @GetMapping("/{id}")
    public String viewRoom(@PathVariable("id") Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        StudyRoom room = roomService.getRoomById(id);

        boolean isMember = roomService.isUserInRoom(id, user);
        if (!isMember && room.isPrivate() && !room.getOwner().equals(user)) {
            // Need access code before joining private room
            model.addAttribute("room", room);
            model.addAttribute("needsAccessCode", true);
            return "rooms/access-code";
        }

        // Auto join if not joined and public
        if (!isMember) {
            try {
                roomService.joinRoom(id, user, null);
            } catch (RoomFullException ex) {
                redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
                return "redirect:/rooms";
            }
        }

        List<RoomMembership> activeMembers = roomService.getActiveRoomMemberships(id);
        List<ChatMessageDto> messages = chatMessageService.getRecentRoomMessages(id, 50);

        model.addAttribute("room", room);
        model.addAttribute("activeMembers", activeMembers);
        model.addAttribute("messages", messages);
        model.addAttribute("isOwner", room.getOwner().equals(user));
        return "rooms/view";
    }

    @PostMapping("/{id}/join")
    public String joinRoomWithCode(@PathVariable("id") Long id,
                                   @RequestParam(value = "accessCode", required = false) String accessCode,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        try {
            roomService.joinRoom(id, user, accessCode);
            return "redirect:/rooms/" + id;
        } catch (UnauthorizedAccessException | RoomFullException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/rooms";
        }
    }

    @PostMapping("/{id}/leave")
    public String leaveRoom(@PathVariable("id") Long id,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        roomService.leaveRoom(id, user);
        redirectAttributes.addFlashAttribute("infoMessage", "You left the study room.");
        return "redirect:/rooms";
    }

    @PostMapping("/{id}/archive")
    public String archiveRoom(@PathVariable("id") Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        try {
            roomService.archiveRoom(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Room has been archived.");
        } catch (UnauthorizedAccessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/rooms";
    }
}
