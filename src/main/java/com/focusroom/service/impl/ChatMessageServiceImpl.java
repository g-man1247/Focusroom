package com.focusroom.service.impl;

import com.focusroom.dto.ChatMessageDto;
import com.focusroom.entity.ChatMessage;
import com.focusroom.entity.StudyRoom;
import com.focusroom.entity.User;
import com.focusroom.entity.enums.MessageType;
import com.focusroom.exception.ResourceNotFoundException;
import com.focusroom.repository.ChatMessageRepository;
import com.focusroom.repository.StudyRoomRepository;
import com.focusroom.service.ChatMessageService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final StudyRoomRepository studyRoomRepository;

    public ChatMessageServiceImpl(ChatMessageRepository chatMessageRepository, StudyRoomRepository studyRoomRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.studyRoomRepository = studyRoomRepository;
    }

    @Override
    @Transactional
    public ChatMessageDto sendMessage(Long roomId, User sender, String message, MessageType messageType) {
        StudyRoom room = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        ChatMessage chatMessage = new ChatMessage(room, sender, message.trim(), messageType);
        ChatMessage saved = chatMessageRepository.save(chatMessage);

        return new ChatMessageDto(
                saved.getId(),
                room.getId(),
                sender.getUsername(),
                sender.getFullName() != null ? sender.getFullName() : sender.getUsername(),
                saved.getMessage(),
                saved.getMessageType(),
                saved.getSentAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getRecentRoomMessages(Long roomId, int limit) {
        List<ChatMessage> messages = chatMessageRepository.findRecentMessagesByRoomId(roomId, PageRequest.of(0, limit));
        return messages.stream()
                .map(m -> new ChatMessageDto(
                        m.getId(),
                        roomId,
                        m.getSender().getUsername(),
                        m.getSender().getFullName() != null ? m.getSender().getFullName() : m.getSender().getUsername(),
                        m.getMessage(),
                        m.getMessageType(),
                        m.getSentAt()
                ))
                .collect(Collectors.toList());
    }
}
