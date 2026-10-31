package com.focusroom.controller;

import com.focusroom.dto.ChatMessageDto;
import com.focusroom.entity.User;
import com.focusroom.entity.enums.MessageType;
import com.focusroom.service.ChatMessageService;
import com.focusroom.service.UserService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketChatController {

    private final ChatMessageService chatMessageService;
    private final UserService userService;

    public WebSocketChatController(ChatMessageService chatMessageService, UserService userService) {
        this.chatMessageService = chatMessageService;
        this.userService = userService;
    }

    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessageDto sendMessage(@DestinationVariable Long roomId,
                                      @Payload ChatMessageDto chatMessageDto,
                                      Principal principal) {
        if (principal == null) {
            return chatMessageDto;
        }
        User sender = userService.findByUsername(principal.getName()).orElse(null);
        if (sender == null) {
            return chatMessageDto;
        }

        return chatMessageService.sendMessage(roomId, sender, chatMessageDto.getMessage(), MessageType.CHAT);
    }

    @MessageMapping("/chat.addUser/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessageDto addUser(@DestinationVariable Long roomId,
                                  @Payload ChatMessageDto chatMessageDto,
                                  Principal principal) {
        if (principal != null) {
            User sender = userService.findByUsername(principal.getName()).orElse(null);
            if (sender != null) {
                return chatMessageService.sendMessage(roomId, sender, "joined the study room", MessageType.JOIN);
            }
        }
        return chatMessageDto;
    }
}
