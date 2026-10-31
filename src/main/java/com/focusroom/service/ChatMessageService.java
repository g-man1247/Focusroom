package com.focusroom.service;

import com.focusroom.dto.ChatMessageDto;
import com.focusroom.entity.ChatMessage;
import com.focusroom.entity.User;
import com.focusroom.entity.enums.MessageType;

import java.util.List;

public interface ChatMessageService {

    ChatMessageDto sendMessage(Long roomId, User sender, String message, MessageType messageType);

    List<ChatMessageDto> getRecentRoomMessages(Long roomId, int limit);
}
